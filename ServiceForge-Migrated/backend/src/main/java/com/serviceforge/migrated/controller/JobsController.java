package com.serviceforge.migrated.controller;

import com.serviceforge.migrated.dto.CreateJobRequest;
import com.serviceforge.migrated.dto.JobDto;
import com.serviceforge.migrated.model.Job;
import com.serviceforge.migrated.service.JobBookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/jobs")
public class JobsController {
    private final JobBookingService booking;

    public JobsController(JobBookingService booking) {
        this.booking = booking;
    }

    @PostMapping
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public ResponseEntity<JobDto> create(@Valid @RequestBody CreateJobRequest req) {
        Job created = booking.createJob(req.technicianId(), req.customerName(), req.startTime(), req.endTime());
        URI location = URI.create("/api/jobs/" + created.id());
        return ResponseEntity.created(location).body(toDto(created));
    }

    private JobDto toDto(Job j) {
        return new JobDto(j.id(), j.technicianId(), j.customerName(), j.startTime().toString(), j.endTime().toString());
    }
}
