package com.serviceforge.controller;

import com.serviceforge.dto.ApiError;
import com.serviceforge.dto.TechnicianResponse;
import com.serviceforge.model.Job;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import com.serviceforge.service.TechnicianAvailabilityService;
import com.serviceforge.service.TechnicianManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

    private final TechnicianAvailabilityService availabilityService;
    private final TechnicianManagementService managementService;

    public TechnicianController(TechnicianAvailabilityService availabilityService, TechnicianManagementService managementService) {
        this.availabilityService = availabilityService;
        this.managementService = managementService;
    }

    @GetMapping
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians(
            @RequestParam(required = false) TechnicianStatus status,
            @RequestParam(required = false) String search) {

        List<TechnicianResponse> results = managementService
                .list(Optional.ofNullable(status), Optional.ofNullable(search))
                .stream()
                .map(TechnicianResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTechnician(@PathVariable Long id) {
        return availabilityService.findTechnician(id)
            .<ResponseEntity<?>>map(t -> ResponseEntity.ok(TechnicianResponse.from(t)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiError(404, "NOT_FOUND", "No technician with id " + id, "Technician not found", null)));
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<?> getJobsForTechnician(@PathVariable Long id) {
        if (availabilityService.findTechnician(id).isEmpty()) {
            return ResponseEntity.status(404).body(new ApiError(404, "NOT_FOUND", "No technician with id " + id, "Technician not found", null));
        }
        List<Job> jobs = availabilityService.getJobsForTechnician(id);
        return ResponseEntity.ok(jobs);
    }
}
