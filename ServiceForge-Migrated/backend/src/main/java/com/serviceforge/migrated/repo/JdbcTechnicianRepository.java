package com.serviceforge.migrated.repo;

import com.serviceforge.migrated.model.Technician;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class JdbcTechnicianRepository {
    private final JdbcTemplate jdbc;

    public JdbcTechnicianRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Technician> listActive() {
        return jdbc.query(
                "SELECT id, name, active, age, area_served, vehicle_make, vehicle_model, vehicle_year, skills_csv FROM technicians WHERE active = 1 ORDER BY name ASC",
                (rs, rowNum) -> new Technician(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("active") == 1,
                        (Integer) rs.getObject("age"),
                        rs.getString("area_served"),
                        rs.getString("vehicle_make"),
                        rs.getString("vehicle_model"),
                        (Integer) rs.getObject("vehicle_year"),
                        rs.getString("skills_csv")
                )
        );
    }

    public List<Technician> list(String q, String status) {
        String query = (q == null) ? "" : q.trim().toLowerCase();
        boolean hasQ = !query.isEmpty();
        String st = (status == null) ? "all" : status.trim().toLowerCase();

        StringBuilder sql = new StringBuilder(
                "SELECT id, name, active, age, area_served, vehicle_make, vehicle_model, vehicle_year, skills_csv FROM technicians WHERE 1=1"
        );

        if ("active".equals(st)) {
            sql.append(" AND active = 1");
        } else if ("inactive".equals(st)) {
            sql.append(" AND active = 0");
        }

        if (hasQ) {
            sql.append(" AND LOWER(name) LIKE ?");
        }

        sql.append(" ORDER BY name ASC");

        if (hasQ) {
            String like = "%" + query + "%";
            return jdbc.query(
                    sql.toString(),
                    (rs, rowNum) -> new Technician(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getInt("active") == 1,
                            (Integer) rs.getObject("age"),
                            rs.getString("area_served"),
                            rs.getString("vehicle_make"),
                            rs.getString("vehicle_model"),
                            (Integer) rs.getObject("vehicle_year"),
                            rs.getString("skills_csv")
                    ),
                    like
            );
        }

        return jdbc.query(
                sql.toString(),
                (rs, rowNum) -> new Technician(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("active") == 1,
                        (Integer) rs.getObject("age"),
                        rs.getString("area_served"),
                        rs.getString("vehicle_make"),
                        rs.getString("vehicle_model"),
                        (Integer) rs.getObject("vehicle_year"),
                        rs.getString("skills_csv")
                )
        );
    }

    public Optional<Technician> findById(long id) {
        List<Technician> rows = jdbc.query(
                "SELECT id, name, active, age, area_served, vehicle_make, vehicle_model, vehicle_year, skills_csv FROM technicians WHERE id = ?",
                (rs, rowNum) -> new Technician(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("active") == 1,
                        (Integer) rs.getObject("age"),
                        rs.getString("area_served"),
                        rs.getString("vehicle_make"),
                        rs.getString("vehicle_model"),
                        (Integer) rs.getObject("vehicle_year"),
                        rs.getString("skills_csv")
                ),
                id
        );
        return rows.stream().findFirst();
    }

    public Technician insert(Technician t) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO technicians(name, active, age, area_served, vehicle_make, vehicle_model, vehicle_year, skills_csv) VALUES(?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, t.name());
            ps.setInt(2, t.active() ? 1 : 0);
            ps.setObject(3, t.age());
            ps.setString(4, t.areaServed());
            ps.setString(5, t.vehicleMake());
            ps.setString(6, t.vehicleModel());
            ps.setObject(7, t.vehicleYear());
            ps.setString(8, t.skillsCsv());
            return ps;
        }, kh);

        Number key = kh.getKey();
        long id = key == null ? -1 : key.longValue();
        return new Technician(id, t.name(), t.active(), t.age(), t.areaServed(), t.vehicleMake(), t.vehicleModel(), t.vehicleYear(), t.skillsCsv());
    }

    public int update(long id, Technician t) {
        return jdbc.update(
                "UPDATE technicians SET name = ?, age = ?, area_served = ?, vehicle_make = ?, vehicle_model = ?, vehicle_year = ?, skills_csv = ? WHERE id = ?",
                t.name(),
                t.age(),
                t.areaServed(),
                t.vehicleMake(),
                t.vehicleModel(),
                t.vehicleYear(),
                t.skillsCsv(),
                id
        );
    }

    public int setActive(long id, boolean active) {
        return jdbc.update("UPDATE technicians SET active = ? WHERE id = ?", active ? 1 : 0, id);
    }
}
