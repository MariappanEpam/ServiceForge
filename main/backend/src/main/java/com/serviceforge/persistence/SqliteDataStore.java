package com.serviceforge.persistence;

import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.model.Technician;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SqliteDataStore {

    private final SqliteConnectionFactory connectionFactory;
    private final SqliteSchemaInitializer schemaInitializer;
    private final SqliteSequenceService sequenceService;
    private final SqliteBootstrapper bootstrapper;
    private final PersistenceProperties props;

    public SqliteDataStore(
            SqliteConnectionFactory connectionFactory,
            SqliteSchemaInitializer schemaInitializer,
            SqliteSequenceService sequenceService,
            SqliteBootstrapper bootstrapper,
            PersistenceProperties props
    ) {
        this.connectionFactory = connectionFactory;
        this.schemaInitializer = schemaInitializer;
        this.sequenceService = sequenceService;
        this.bootstrapper = bootstrapper;
        this.props = props;

        // Initialize schema + seed on startup
        try (Connection c = open()) {
            schemaInitializer.ensureSchema(c);
            bootstrapper.seedIfEmpty(c, sequenceService);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize SQLite datastore", e);
        }
    }

    private Connection open() {
        return connectionFactory.open(props.getSqlitePath());
    }

    public List<Technician> getAllTechnicians() {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, region FROM technicians ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Technician> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Technician(rs.getLong(1), rs.getString(2), rs.getString(3)));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list technicians", e);
        }
    }

    public Optional<Technician> findTechnician(Long id) {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, region FROM technicians WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Technician(rs.getLong(1), rs.getString(2), rs.getString(3)));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find technician " + id, e);
        }
    }

    public Technician createTechnician(Technician technician) {
        try (Connection c = open()) {
            long id = sequenceService.next(c, "technician");
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO technicians(id, name, region) VALUES(?,?,?)")) {
                ps.setLong(1, id);
                ps.setString(2, technician.getName());
                ps.setString(3, technician.getRegion());
                ps.executeUpdate();
            }
            return new Technician(id, technician.getName(), technician.getRegion());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create technician", e);
        }
    }

    public List<Job> getJobsForTechnician(Long technicianId) {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT id, technician_id, customer_name, start_time, end_time, status FROM jobs WHERE technician_id = ? ORDER BY start_time")) {
            ps.setLong(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Job> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapJob(rs));
                }
                return out;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list jobs for technician " + technicianId, e);
        }
    }

    public List<Job> getAllJobs() {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT id, technician_id, customer_name, start_time, end_time, status FROM jobs ORDER BY start_time");
             ResultSet rs = ps.executeQuery()) {
            List<Job> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapJob(rs));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list jobs", e);
        }
    }

    public Job saveJob(Job job) {
        try (Connection c = open()) {
            long id = sequenceService.next(c, "job");
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES(?,?,?,?,?,?)")) {
                ps.setLong(1, id);
                ps.setLong(2, job.getTechnicianId());
                ps.setString(3, job.getCustomerName());
                ps.setString(4, job.getStartTime().toString());
                ps.setString(5, job.getEndTime().toString());
                ps.setString(6, job.getStatus().name());
                ps.executeUpdate();
            }
            return new Job(id, job.getTechnicianId(), job.getCustomerName(), job.getStartTime(), job.getEndTime(), job.getStatus());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save job", e);
        }
    }

    private Job mapJob(ResultSet rs) throws Exception {
        Long id = rs.getLong(1);
        Long techId = rs.getLong(2);
        String customer = rs.getString(3);
        LocalDateTime start = LocalDateTime.parse(rs.getString(4));
        LocalDateTime end = LocalDateTime.parse(rs.getString(5));
        JobStatus status = JobStatus.valueOf(rs.getString(6));
        return new Job(id, techId, customer, start, end, status);
    }
}
