package com.serviceforge.migrated.service;

import com.serviceforge.migrated.api.BadRequestException;
import com.serviceforge.migrated.api.NotFoundException;
import com.serviceforge.migrated.model.Job;
import com.serviceforge.migrated.model.Technician;
import com.serviceforge.migrated.repo.JdbcJobRepository;
import com.serviceforge.migrated.repo.JdbcTechnicianRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TechnicianScheduleService {
    private final JdbcTechnicianRepository technicians;
    private final JdbcJobRepository jobs;

    public TechnicianScheduleService(JdbcTechnicianRepository technicians, JdbcJobRepository jobs) {
        this.technicians = technicians;
        this.jobs = jobs;
    }

    public List<Technician> listTechnicians() {
        return technicians.listActive();
    }

    public List<Job> listJobs(long technicianId, String fromIso, String toIso) {
        Technician tech = technicians.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND"));

        Instant from;
        Instant to;

        if ((fromIso == null || fromIso.isBlank()) && (toIso == null || toIso.isBlank())) {
            from = TimePolicy.startOfCurrentWeekUtc();
            to = TimePolicy.endOfCurrentWeekUtcExclusive();
        } else if (fromIso == null || fromIso.isBlank() || toIso == null || toIso.isBlank()) {
            throw new BadRequestException("Both 'from' and 'to' must be provided together", "INVALID_WINDOW");
        } else {
            from = TimePolicy.parseIsoToUtcInstant(fromIso);
            to = TimePolicy.parseIsoToUtcInstant(toIso);
        }

        if (!to.isAfter(from)) {
            throw new BadRequestException("'to' must be after 'from'", "INVALID_WINDOW");
        }

        // tech is currently unused beyond existence check; keep for future authorization rules.
        return jobs.findByTechnicianAndWindow(tech.id(), from, to);
    }
}
