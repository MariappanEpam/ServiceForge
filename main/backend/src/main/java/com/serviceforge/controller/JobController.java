package com.serviceforge.controller;

import com.serviceforge.dto.ApiError;
import com.serviceforge.dto.BookJobRequest;
import com.serviceforge.model.Job;
import com.serviceforge.service.TechnicianAvailabilityService;
import com.serviceforge.service.IReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final IReservationService availabilityService;

    public JobController(IReservationService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<?> bookJob(@Valid @RequestBody BookJobRequest request, org.springframework.web.context.request.WebRequest webRequest) {
        Job job = availabilityService.bookJob(
                request.getTechnicianId(),
                request.getCustomerName(),
                request.getStartTime(),
                request.getEndTime());
        return ResponseEntity.status(201).body(job);
    }
}
