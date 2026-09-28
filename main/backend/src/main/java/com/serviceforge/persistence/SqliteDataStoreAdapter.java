package com.serviceforge.persistence;

import com.serviceforge.model.Job;
import com.serviceforge.model.Technician;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SqliteDataStoreAdapter implements ServiceForgeDataStore {

    private final SqliteDataStore sqlite;

    public SqliteDataStoreAdapter(SqliteDataStore sqlite) {
        this.sqlite = sqlite;
    }

    @Override
    public List<Technician> getAllTechnicians() {
        return sqlite.getAllTechnicians();
    }

    @Override
    public Optional<Technician> findTechnician(Long id) {
        return sqlite.findTechnician(id);
    }

    @Override
    public List<Job> getJobsForTechnician(Long technicianId) {
        return sqlite.getJobsForTechnician(technicianId);
    }

    @Override
    public List<Job> getAllJobs() {
        return sqlite.getAllJobs();
    }

    @Override
    public Job saveJob(Job job) {
        return sqlite.saveJob(job);
    }

    @Override
    public long nextJobId() {
        // SQLite assigns IDs via saveJob; keep this for interface compatibility.
        return -1;
    }
}
