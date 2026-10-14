package com.serviceforge.migrated.repo;

import com.serviceforge.migrated.model.Job;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
public class JdbcJobRepository {
    private final JdbcTemplate jdbc;

    public JdbcJobRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Job> findByTechnicianAndWindow(long technicianId, Instant from, Instant to) {
        return jdbc.query(
                "SELECT id, technician_id, customer_name, start_time, end_time FROM jobs WHERE technician_id = ? AND start_time >= ? AND end_time <= ? ORDER BY start_time ASC",
                (rs, rowNum) -> new Job(
                        rs.getLong("id"),
                        rs.getLong("technician_id"),
                        rs.getString("customer_name"),
                        Instant.parse(rs.getString("start_time")),
                        Instant.parse(rs.getString("end_time"))
                ),
                technicianId,
                from.toString(),
                to.toString()
        );
    }

    public boolean existsOverlapping(long technicianId, Instant expandedStart, Instant expandedEnd) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM jobs WHERE technician_id = ? AND start_time < ? AND end_time > ?",
                Integer.class,
                technicianId,
                expandedEnd.toString(),
                expandedStart.toString()
        );
        return count != null && count > 0;
    }

    public Job insert(long technicianId, String customerName, Instant start, Instant end) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO jobs(technician_id, customer_name, start_time, end_time) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, technicianId);
            ps.setString(2, customerName);
            ps.setString(3, start.toString());
            ps.setString(4, end.toString());
            return ps;
        }, kh);

        Number key = kh.getKey();
        long id = key == null ? -1 : key.longValue();
        return new Job(id, technicianId, customerName, start, end);
    }
}
