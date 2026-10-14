package com.serviceforge.migrated.controller;

import com.serviceforge.migrated.dto.JobDto;
import com.serviceforge.migrated.dto.TechnicianDto;
import com.serviceforge.migrated.dto.TechnicianSummaryDto;
import com.serviceforge.migrated.dto.TechnicianUpsertRequest;
import com.serviceforge.migrated.dto.VehicleDetailsDto;
import com.serviceforge.migrated.model.Job;
import com.serviceforge.migrated.model.Technician;
import com.serviceforge.migrated.service.TechnicianManagementService;
import com.serviceforge.migrated.service.TechnicianScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/technicians")
public class TechniciansController {
    private final TechnicianScheduleService schedule;
    private final TechnicianManagementService management;

    public TechniciansController(TechnicianScheduleService schedule, TechnicianManagementService management) {
        this.schedule = schedule;
        this.management = management;
    }

    @GetMapping
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<TechnicianSummaryDto> listTechnicians() {
        return schedule.listTechnicians().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping(params = {"q", "status"})
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<TechnicianDto> listManagedWithQueryAndStatus(@RequestParam String q, @RequestParam String status) {
        return management.list(q, status).stream().map(this::toTechnicianDto).toList();
    }

    // Feature 9: management endpoints

    @GetMapping(params = {"status"})
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<TechnicianDto> listManagedByStatus(@RequestParam String status) {
        return management.list(null, status).stream().map(this::toTechnicianDto).toList();
    }

    @GetMapping(params = {"q"})
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<TechnicianDto> listManagedByQuery(@RequestParam String q) {
        return management.list(q, null).stream().map(this::toTechnicianDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public TechnicianDto get(@PathVariable long id) {
        return toTechnicianDto(management.get(id));
    }

    @PostMapping
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public ResponseEntity<TechnicianDto> create(@Valid @RequestBody TechnicianUpsertRequest req) {
        Technician created = management.create(req);
        return ResponseEntity.created(URI.create("/api/technicians/" + created.id())).body(toTechnicianDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public TechnicianDto update(@PathVariable long id, @Valid @RequestBody TechnicianUpsertRequest req) {
        return toTechnicianDto(management.update(id, req));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public TechnicianDto deactivate(@PathVariable long id) {
        return toTechnicianDto(management.deactivate(id));
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public TechnicianDto reactivate(@PathVariable long id) {
        return toTechnicianDto(management.reactivate(id));
    }

    @GetMapping("/{technicianId}/jobs")
        @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<JobDto> listJobs(
            @PathVariable long technicianId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return schedule.listJobs(technicianId, from, to).stream()
                .map(this::toJobDto)
                .toList();
    }

    private TechnicianSummaryDto toSummary(Technician t) {
        return new TechnicianSummaryDto(t.id(), t.name());
    }

    private TechnicianDto toTechnicianDto(Technician t) {
        VehicleDetailsDto vehicle = null;
        if (t.vehicleMake() != null || t.vehicleModel() != null || t.vehicleYear() != null) {
            vehicle = new VehicleDetailsDto(t.vehicleMake(), t.vehicleModel(), t.vehicleYear());
        }

        List<String> skills = List.of();
        if (t.skillsCsv() != null && !t.skillsCsv().trim().isEmpty()) {
            skills = Arrays.stream(t.skillsCsv().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return new TechnicianDto(
                t.id(),
                t.name(),
                t.active(),
                t.age(),
                t.areaServed(),
                vehicle,
                skills
        );
    }

    private JobDto toJobDto(Job j) {
        return new JobDto(j.id(), j.technicianId(), j.customerName(), j.startTime().toString(), j.endTime().toString());
    }
}
