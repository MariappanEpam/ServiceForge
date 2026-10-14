package com.serviceforge.migrated.repo;

import com.serviceforge.migrated.model.Part;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcPartRepository {
    private final JdbcTemplate jdbc;

    public JdbcPartRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Part> listActiveOrderable() {
        return jdbc.query(
                "SELECT sku, name, description, active, orderable, quantity_on_hand FROM parts WHERE active = 1 AND orderable = 1 ORDER BY name ASC",
                (rs, rowNum) -> new Part(
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("active") == 1,
                        rs.getInt("orderable") == 1,
                        rs.getInt("quantity_on_hand")
                )
        );
    }

        public List<Part> list(String q, boolean activeOnly) {
        String query = (q == null) ? "" : q.trim().toLowerCase();
        boolean hasQ = !query.isEmpty();

        StringBuilder sql = new StringBuilder(
            "SELECT sku, name, description, active, orderable, quantity_on_hand FROM parts WHERE 1=1"
        );
        if (activeOnly) {
            sql.append(" AND active = 1");
        }
        if (hasQ) {
            sql.append(" AND (LOWER(sku) LIKE ? OR LOWER(name) LIKE ?)");
        }
        sql.append(" ORDER BY name ASC");

        if (hasQ) {
            String like = "%" + query + "%";
            return jdbc.query(
                sql.toString(),
                (rs, rowNum) -> new Part(
                    rs.getString("sku"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("active") == 1,
                    rs.getInt("orderable") == 1,
                    rs.getInt("quantity_on_hand")
                ),
                like,
                like
            );
        }

        return jdbc.query(
            sql.toString(),
            (rs, rowNum) -> new Part(
                rs.getString("sku"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("active") == 1,
                rs.getInt("orderable") == 1,
                rs.getInt("quantity_on_hand")
            )
        );
        }

        public Part insert(Part part) {
        jdbc.update(
            "INSERT INTO parts (sku, name, description, active, orderable, quantity_on_hand) VALUES (?,?,?,?,?,?)",
            part.sku(),
            part.name(),
            part.description(),
            part.active() ? 1 : 0,
            part.orderable() ? 1 : 0,
            part.quantityOnHand()
        );
        return part;
        }

        public int update(String sku, Part part) {
        return jdbc.update(
            "UPDATE parts SET name = ?, description = ?, active = ?, orderable = ?, quantity_on_hand = ? WHERE sku = ?",
            part.name(),
            part.description(),
            part.active() ? 1 : 0,
            part.orderable() ? 1 : 0,
            part.quantityOnHand(),
            sku
        );
        }

        public int softDelete(String sku) {
        return jdbc.update("UPDATE parts SET active = 0 WHERE sku = ?", sku);
        }

        public Part getBySku(String sku) {
        List<Part> rows = jdbc.query(
            "SELECT sku, name, description, active, orderable, quantity_on_hand FROM parts WHERE sku = ?",
            (rs, rowNum) -> new Part(
                rs.getString("sku"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("active") == 1,
                rs.getInt("orderable") == 1,
                rs.getInt("quantity_on_hand")
            ),
            sku
        );
        return rows.isEmpty() ? null : rows.get(0);
        }
}
