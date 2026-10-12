package com.serviceforge.service;

import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import org.springframework.stereotype.Service;

@Service
public class TechnicianLifecycleService {

    public void transition(Technician technician, TechnicianStatus target) {
        TechnicianStatus current = technician.getStatus();
        if (current == null) {
            current = TechnicianStatus.ACTIVE;
        }

        boolean allowed = switch (target) {
            case ACTIVE -> current == TechnicianStatus.SUSPENDED;
            case SUSPENDED -> current == TechnicianStatus.ACTIVE;
            case OFFBOARDED -> current == TechnicianStatus.INVITED
                    || current == TechnicianStatus.ACTIVE
                    || current == TechnicianStatus.SUSPENDED;
            case INVITED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException("Invalid technician status transition: " + current + " -> " + target);
        }

        technician.setStatus(target);
    }

    public void requireBookable(Technician technician) {
        TechnicianStatus status = technician.getStatus();
        if (status != TechnicianStatus.ACTIVE) {
            throw new IllegalStateException("Technician is not ACTIVE and cannot be booked");
        }
    }
}
