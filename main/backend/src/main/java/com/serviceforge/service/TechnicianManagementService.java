package com.serviceforge.service;

import com.serviceforge.dto.CreateTechnicianRequest;
import com.serviceforge.dto.UpdateTechnicianRequest;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import com.serviceforge.persistence.ServiceForgeDataStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TechnicianManagementService {

    private final ServiceForgeDataStore dataStore;
    private final com.serviceforge.data.MockDataStore mockDataStore;

    public TechnicianManagementService(ServiceForgeDataStore dataStore, com.serviceforge.data.MockDataStore mockDataStore) {
        this.dataStore = dataStore;
        this.mockDataStore = mockDataStore;
    }

    public List<Technician> list(Optional<TechnicianStatus> status, Optional<String> search) {
        String q = search.map(s -> s.toLowerCase(Locale.ROOT).trim()).orElse(null);

        return dataStore.getAllTechnicians().stream()
                .filter(t -> status.map(st -> st == t.getStatus()).orElse(true))
                .filter(t -> {
                    if (q == null || q.isBlank()) {
                        return true;
                    }
                    String name = t.getName() == null ? "" : t.getName().toLowerCase(Locale.ROOT);
                    String email = t.getEmail() == null ? "" : t.getEmail().toLowerCase(Locale.ROOT);
                    String phone = t.getPhone() == null ? "" : t.getPhone().toLowerCase(Locale.ROOT);
                    return name.contains(q) || email.contains(q) || phone.contains(q);
                })
                .sorted(Comparator.comparing(Technician::getId))
                .collect(Collectors.toList());
    }

    public Technician getRequired(Long id) {
        return dataStore.findTechnician(id)
                .orElseThrow(() -> new IllegalArgumentException("No technician with id " + id));
    }

    public Technician create(CreateTechnicianRequest req, long id) {
        Technician t = new Technician(id, req.getName(), req.getRegion(), TechnicianStatus.INVITED, req.getTrade(), req.getEmail(), req.getPhone());
        return t;
    }

    public Technician update(Technician t, UpdateTechnicianRequest req) {
        t.setName(req.getName());
        t.setRegion(req.getRegion());
        t.setTrade(req.getTrade());
        t.setEmail(req.getEmail());
        t.setPhone(req.getPhone());
        return t;
    }
}
