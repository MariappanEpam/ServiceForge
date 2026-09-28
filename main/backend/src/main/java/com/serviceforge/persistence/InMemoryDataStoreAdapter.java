package com.serviceforge.persistence;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.Job;
import com.serviceforge.model.Technician;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InMemoryDataStoreAdapter implements ServiceForgeDataStore {

    private final MockDataStore dataStore;

    public InMemoryDataStoreAdapter(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Technician> getAllTechnicians() {
        return dataStore.getAllTechnicians();
    }

    @Override
    public Optional<Technician> findTechnician(Long id) {
        return dataStore.findTechnician(id);
    }

    @Override
    public List<Job> getJobsForTechnician(Long technicianId) {
        return dataStore.getJobsForTechnician(technicianId);
    }

    @Override
    public List<Job> getAllJobs() {
        return dataStore.getAllJobs();
    }

    @Override
    public Job saveJob(Job job) {
        return dataStore.save(job);
    }

    @Override
    public long nextJobId() {
        return dataStore.nextJobId();
    }
}
