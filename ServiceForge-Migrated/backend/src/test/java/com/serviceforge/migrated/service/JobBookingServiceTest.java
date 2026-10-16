package com.serviceforge.migrated.service;

import com.serviceforge.migrated.api.ConflictException;
import com.serviceforge.migrated.api.NotFoundException;
import com.serviceforge.migrated.repo.JdbcJobRepository;
import com.serviceforge.migrated.repo.JdbcTechnicianRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JobBookingServiceTest {

    @Test
    void createJob_throwsNotFound_whenTechnicianMissing() {
        JdbcTechnicianRepository techRepo = Mockito.mock(JdbcTechnicianRepository.class);
        JdbcJobRepository jobRepo = Mockito.mock(JdbcJobRepository.class);
        Mockito.when(techRepo.findById(99L)).thenReturn(Optional.empty());

        JobBookingService svc = new JobBookingService(techRepo, jobRepo);

        assertThrows(NotFoundException.class, () -> svc.createJob(99L, "Acme", "2026-10-14T10:00:00Z", "2026-10-14T11:00:00Z"));
    }

    @Test
    void createJob_throwsConflict_whenOverlapDetected() {
        JdbcTechnicianRepository techRepo = Mockito.mock(JdbcTechnicianRepository.class);
        JdbcJobRepository jobRepo = Mockito.mock(JdbcJobRepository.class);
        Mockito.when(techRepo.findById(1L)).thenReturn(Optional.of(new com.serviceforge.migrated.model.Technician(
            1L,
            "Alex",
            true,
            30,
            "North",
            "Ford",
            "Transit",
            2021,
            "HVAC"
        )));
        Mockito.when(jobRepo.existsOverlapping(Mockito.eq(1L), Mockito.any(Instant.class), Mockito.any(Instant.class))).thenReturn(true);

        JobBookingService svc = new JobBookingService(techRepo, jobRepo);

        assertThrows(ConflictException.class, () -> svc.createJob(1L, "Acme", "2026-10-14T10:00:00Z", "2026-10-14T11:00:00Z"));
    }
}
