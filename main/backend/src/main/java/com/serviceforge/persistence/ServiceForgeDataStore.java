package com.serviceforge.persistence;

import com.serviceforge.model.Job;
import com.serviceforge.model.Technician;

import java.util.List;
import java.util.Optional;

public interface ServiceForgeDataStore {
    List<Technician> getAllTechnicians();

    Optional<Technician> findTechnician(Long id);

    Technician createTechnician(Technician technician);

    long nextTechnicianId();

    List<Job> getJobsForTechnician(Long technicianId);

    List<Job> getAllJobs();

    Job saveJob(Job job);

    long nextJobId();
}
