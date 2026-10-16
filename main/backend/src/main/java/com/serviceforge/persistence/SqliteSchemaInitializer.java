package com.serviceforge.persistence;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class SqliteSchemaInitializer {

    public void ensureSchema(Connection c) {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("PRAGMA foreign_keys = ON");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS technicians (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "region TEXT NOT NULL" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS jobs (" +
                    "id INTEGER PRIMARY KEY, " +
                    "technician_id INTEGER NOT NULL, " +
                    "customer_name TEXT NOT NULL, " +
                    "start_time TEXT NOT NULL, " +
                    "end_time TEXT NOT NULL, " +
                    "status TEXT NOT NULL" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS inventory (" +
                    "sku TEXT PRIMARY KEY, " +
                    "quantity INTEGER NOT NULL" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS part_reservations (" +
                    "id INTEGER PRIMARY KEY, " +
                    "sku TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "job_id INTEGER, " +
                    "technician_id INTEGER, " +
                    "requested_quantity INTEGER NOT NULL, " +
                    "shortage INTEGER NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "status TEXT NOT NULL" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS order_requests (" +
                    "id INTEGER PRIMARY KEY, " +
                    "sku TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "reservation_id INTEGER NOT NULL, " +
                    "status TEXT NOT NULL" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS sequences (" +
                    "name TEXT PRIMARY KEY, " +
                    "value INTEGER NOT NULL" +
                    ")");

            // seed sequences if missing
            st.executeUpdate("INSERT OR IGNORE INTO sequences(name, value) VALUES('job', 1)");
            st.executeUpdate("INSERT OR IGNORE INTO sequences(name, value) VALUES('reservation', 1)");
            st.executeUpdate("INSERT OR IGNORE INTO sequences(name, value) VALUES('order', 1)");

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize SQLite schema", e);
        }
    }
}
