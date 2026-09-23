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
        try {
            Job job = availabilityService.bookJob(
                    request.getTechnicianId(),
                    request.getCustomerName(),
                    request.getStartTime(),
                    request.getEndTime());
            return ResponseEntity.status(201).body(job);
        } catch (IllegalArgumentException e) {
            // let GlobalExceptionHandler handle via IllegalArgumentException -> 400? For now map to 404
            ApiError err = new ApiError(404, "NOT_FOUND", e.getMessage(), e.getMessage(), null);
            return ResponseEntity.status(404).body(err);
        } catch (IllegalStateException e) {
            ApiError err = new ApiError(409, "CONFLICT", e.getMessage(), e.getMessage(), null);
            return ResponseEntity.status(409).body(err);
        }
    }
}
