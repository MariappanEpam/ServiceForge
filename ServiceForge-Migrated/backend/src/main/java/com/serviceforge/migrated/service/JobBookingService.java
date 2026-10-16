package com.serviceforge.migrated.service;

import com.serviceforge.migrated.api.BadRequestException;
import com.serviceforge.migrated.api.ConflictException;
import com.serviceforge.migrated.api.NotFoundException;
import com.serviceforge.migrated.model.Job;
import com.serviceforge.migrated.repo.JdbcJobRepository;
import com.serviceforge.migrated.repo.JdbcTechnicianRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JobBookingService {
    private static final Duration TRAVEL_BUFFER = Duration.ofMinutes(45);

    private final JdbcTechnicianRepository technicians;
    private final JdbcJobRepository jobs;

    public JobBookingService(JdbcTechnicianRepository technicians, JdbcJobRepository jobs) {
        this.technicians = technicians;
        this.jobs = jobs;
    }

    public Job createJob(long technicianId, String customerName, String startIso, String endIso) {
        if (customerName == null || customerName.isBlank()) {
            throw new BadRequestException("customerName must not be blank", "INVALID_CUSTOMER_NAME");
        }

        technicians.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND"));

        Instant start = TimePolicy.parseIsoToUtcInstant(startIso);
        Instant end = TimePolicy.parseIsoToUtcInstant(endIso);

        if (!end.isAfter(start)) {
            throw new BadRequestException("endTime must be after startTime", "INVALID_TIME_RANGE");
        }

        Instant expandedStart = start.minus(TRAVEL_BUFFER);
        Instant expandedEnd = end.plus(TRAVEL_BUFFER);

        if (jobs.existsOverlapping(technicianId, expandedStart, expandedEnd)) {
            throw new ConflictException("Requested time conflicts with existing job", "JOB_CONFLICT");
        }

        return jobs.insert(technicianId, customerName.trim(), start, end);
    }
}
