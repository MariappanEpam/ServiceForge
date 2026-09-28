package com.serviceforge.persistence;

import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.model.Technician;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@Component
public class SqliteBootstrapper {

    public void seedIfEmpty(Connection c, SqliteSequenceService seq) {
        try {
            boolean hasTech;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM technicians LIMIT 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    hasTech = rs.next();
                }
            }

            if (!hasTech) {
                insertTechnician(c, new Technician(1L, "Jordan Reyes", "North"));
                insertTechnician(c, new Technician(2L, "Priya Nair", "South"));
                insertTechnician(c, new Technician(3L, "Sam Okafor", "East"));
            }

            boolean hasJobs;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM jobs LIMIT 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    hasJobs = rs.next();
                }
            }

            if (!hasJobs) {
                LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
                insertJob(c, seq, new Job(null, 1L, "Acme Corp",
                        now.withHour(9).withMinute(0),
                        now.withHour(11).withMinute(0),
                        JobStatus.SCHEDULED));
                insertJob(c, seq, new Job(null, 1L, "Northwind Traders",
                        now.withHour(14).withMinute(0),
                        now.withHour(15).withMinute(30),
                        JobStatus.SCHEDULED));
                insertJob(c, seq, new Job(null, 2L, "Globex",
                        now.withHour(10).withMinute(0),
                        now.withHour(12).withMinute(0),
                        JobStatus.SCHEDULED));
            }

            boolean hasInventory;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM inventory LIMIT 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    hasInventory = rs.next();
                }
            }

            if (!hasInventory) {
                upsertInventory(c, "PART-001", 10);
                upsertInventory(c, "PART-002", 5);
                upsertInventory(c, "SKU-1000", 3);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to bootstrap SQLite data", e);
        }
    }

    private void insertTechnician(Connection c, Technician t) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO technicians(id, name, region) VALUES(?,?,?)")) {
            ps.setLong(1, t.getId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getRegion());
            ps.executeUpdate();
        }
    }

    private void insertJob(Connection c, SqliteSequenceService seq, Job job) throws Exception {
        long id = seq.next(c, "job");
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES(?,?,?,?,?,?)")) {
            ps.setLong(1, id);
            ps.setLong(2, job.getTechnicianId());
            ps.setString(3, job.getCustomerName());
            ps.setString(4, job.getStartTime().toString());
            ps.setString(5, job.getEndTime().toString());
            ps.setString(6, job.getStatus().name());
            ps.executeUpdate();
        }
    }

    private void upsertInventory(Connection c, String sku, int qty) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO inventory(sku, quantity) VALUES(?, ?) ON CONFLICT(sku) DO UPDATE SET quantity = excluded.quantity")) {
            ps.setString(1, sku);
            ps.setInt(2, qty);
            ps.executeUpdate();
        }
    }
}
