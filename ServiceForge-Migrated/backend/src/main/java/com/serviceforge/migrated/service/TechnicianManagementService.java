package com.serviceforge.migrated.service;

import com.serviceforge.migrated.api.NotFoundException;
import com.serviceforge.migrated.dto.TechnicianUpsertRequest;
import com.serviceforge.migrated.dto.VehicleDetailsDto;
import com.serviceforge.migrated.model.Technician;
import com.serviceforge.migrated.repo.JdbcTechnicianRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianManagementService {
    private final JdbcTechnicianRepository technicians;

    public TechnicianManagementService(JdbcTechnicianRepository technicians) {
        this.technicians = technicians;
    }

    public List<Technician> list(String q, String status) {
        return technicians.list(q, status);
    }

    public Technician get(long id) {
        return technicians.findById(id)
                .orElseThrow(() -> new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND"));
    }

    public Technician create(TechnicianUpsertRequest req) {
        Technician t = toModel(-1, true, req);
        return technicians.insert(t);
    }

    public Technician update(long id, TechnicianUpsertRequest req) {
        Technician existing = get(id);
        Technician updated = toModel(existing.id(), existing.active(), req);
        int rows = technicians.update(id, updated);
        if (rows == 0) {
            throw new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND");
        }
        return get(id);
    }

    public Technician deactivate(long id) {
        int rows = technicians.setActive(id, false);
        if (rows == 0) {
            throw new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND");
        }
        return get(id);
    }

    public Technician reactivate(long id) {
        int rows = technicians.setActive(id, true);
        if (rows == 0) {
            throw new NotFoundException("Technician not found", "TECHNICIAN_NOT_FOUND");
        }
        return get(id);
    }

    private Technician toModel(long id, boolean active, TechnicianUpsertRequest req) {
        VehicleDetailsDto v = req.vehicle();
        String skillsCsv = null;
        if (req.skills() != null && !req.skills().isEmpty()) {
            skillsCsv = req.skills().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.joining(","));
        }

        return new Technician(
                id,
                req.name().trim(),
                active,
                req.age(),
                req.areaServed().trim(),
                v == null ? null : v.make(),
                v == null ? null : v.model(),
                v == null ? null : v.year(),
                skillsCsv
        );
    }
}
