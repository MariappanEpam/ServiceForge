package com.serviceforge.persistence;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class SqliteSequenceService {

    public synchronized long next(Connection c, String name) {
        try {
            long current;
            try (PreparedStatement ps = c.prepareStatement("SELECT value FROM sequences WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Missing sequence: " + name);
                    }
                    current = rs.getLong(1);
                }
            }

            long next = current + 1;
            try (PreparedStatement ps = c.prepareStatement("UPDATE sequences SET value = ? WHERE name = ?")) {
                ps.setLong(1, next);
                ps.setString(2, name);
                ps.executeUpdate();
            }

            return current;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to increment sequence: " + name, e);
        }
    }
}
