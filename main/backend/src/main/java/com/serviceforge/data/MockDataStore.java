package com.serviceforge.data;

import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory mock data store. There is no real database in this repo (see AGENTS.md) —
 * everything here resets on restart, which is expected for a training boilerplate.
 */
@Component
public class MockDataStore {

    private final List<Technician> technicians = new ArrayList<>();
    private final List<Job> jobs = new ArrayList<>();
    private final AtomicLong jobIdSequence = new AtomicLong(1);
    private final AtomicLong technicianIdSequence = new AtomicLong(4);

    // Feature 3 — onboarding tokens (in-memory only)
    private final Map<String, Long> invitationTokenToTechnicianId = new ConcurrentHashMap<>();

    @PostConstruct
    public void seed() {
        // Default seeding is deterministic relative to 'today' — expose a helper for tests to call
        technicians.add(new Technician(1L, "Jordan Reyes", "North"));
        technicians.add(new Technician(2L, "Priya Nair", "South"));
        technicians.add(new Technician(3L, "Sam Okafor", "East"));

        // Feature 3: seed additional technicians across lifecycle states for demo/testing.
        technicians.add(new Technician(4L, "Alex Chen", "West", TechnicianStatus.INVITED, "HVAC", "alex.chen@example.com", "555-0100"));
        technicians.add(new Technician(5L, "Mina Patel", "North", TechnicianStatus.ACTIVE, "Electrical", "mina.patel@example.com", "555-0101"));
        technicians.add(new Technician(6L, "Diego Santos", "South", TechnicianStatus.SUSPENDED, "Plumbing", "diego.santos@example.com", "555-0102"));
        technicians.add(new Technician(7L, "Taylor Kim", "East", TechnicianStatus.OFFBOARDED, "Appliance", "taylor.kim@example.com", "555-0103"));
        technicians.add(new Technician(8L, "Noah Williams", "West", TechnicianStatus.ACTIVE, "HVAC", "noah.williams@example.com", "555-0104"));
        technicians.add(new Technician(9L, "Aisha Khan", "North", TechnicianStatus.INVITED, "Electrical", "aisha.khan@example.com", "555-0105"));
        technicians.add(new Technician(10L, "Liam O'Connor", "South", TechnicianStatus.SUSPENDED, "Plumbing", "liam.oconnor@example.com", "555-0106"));

        // Seed a couple of non-overlapping jobs so the calendar isn't empty on first run.
        jobs.add(new Job(nextJobId(), 1L, "Acme Corp",
                LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
        jobs.add(new Job(nextJobId(), 1L, "Northwind Traders",
                LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().withHour(15).withMinute(30).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
        jobs.add(new Job(nextJobId(), 2L, "Globex", LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
    }

    public List<Technician> getAllTechnicians() {
        return Collections.unmodifiableList(technicians);
    }

    public Optional<Technician> findTechnician(Long id) {
        return technicians.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Technician createTechnician(Technician technician) {
        technicians.add(technician);
        return technician;
    }

    public long nextTechnicianId() {
        return technicianIdSequence.getAndIncrement();
    }

    public void indexInvitationToken(String token, Long technicianId) {
        invitationTokenToTechnicianId.put(token, technicianId);
    }

    public void removeInvitationToken(String token) {
        invitationTokenToTechnicianId.remove(token);
    }

    public Optional<Technician> findTechnicianByInvitationToken(String token) {
        Long id = invitationTokenToTechnicianId.get(token);
        if (id == null) {
            return Optional.empty();
        }
        return findTechnician(id);
    }

    public List<Job> getJobsForTechnician(Long technicianId) {
        return jobs.stream()
                .filter(j -> j.getTechnicianId().equals(technicianId))
                .collect(Collectors.toList());
    }

    public List<Job> getAllJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public Job save(Job job) {
        jobs.add(job);
        return job;
    }

    public Long nextJobId() {
        return jobIdSequence.getAndIncrement();
    }

    // Test helper: reset seed to a deterministic fixed-day schedule for tests.
    public void seedDeterministic(LocalDateTime baseDate) {
        technicians.clear();
        jobs.clear();
        jobIdSequence.set(1);
        technicianIdSequence.set(4);
        invitationTokenToTechnicianId.clear();

        technicians.add(new Technician(1L, "Jordan Reyes", "North"));
        technicians.add(new Technician(2L, "Priya Nair", "South"));
        technicians.add(new Technician(3L, "Sam Okafor", "East"));

        // Feature 3: deterministic seed across lifecycle states.
        technicians.add(new Technician(4L, "Alex Chen", "West", TechnicianStatus.INVITED, "HVAC", "alex.chen@example.com", "555-0100"));
        technicians.add(new Technician(5L, "Mina Patel", "North", TechnicianStatus.ACTIVE, "Electrical", "mina.patel@example.com", "555-0101"));
        technicians.add(new Technician(6L, "Diego Santos", "South", TechnicianStatus.SUSPENDED, "Plumbing", "diego.santos@example.com", "555-0102"));
        technicians.add(new Technician(7L, "Taylor Kim", "East", TechnicianStatus.OFFBOARDED, "Appliance", "taylor.kim@example.com", "555-0103"));
        technicians.add(new Technician(8L, "Noah Williams", "West", TechnicianStatus.ACTIVE, "HVAC", "noah.williams@example.com", "555-0104"));
        technicians.add(new Technician(9L, "Aisha Khan", "North", TechnicianStatus.INVITED, "Electrical", "aisha.khan@example.com", "555-0105"));
        technicians.add(new Technician(10L, "Liam O'Connor", "South", TechnicianStatus.SUSPENDED, "Plumbing", "liam.oconnor@example.com", "555-0106"));

        jobs.add(new Job(nextJobId(), 1L, "Acme Corp",
                baseDate.withHour(9).withMinute(0).withSecond(0).withNano(0),
                baseDate.withHour(11).withMinute(0).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
        jobs.add(new Job(nextJobId(), 1L, "Northwind Traders",
                baseDate.withHour(14).withMinute(0).withSecond(0).withNano(0),
                baseDate.withHour(15).withMinute(30).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
        jobs.add(new Job(nextJobId(), 2L, "Globex", baseDate.withHour(10).withMinute(0).withSecond(0).withNano(0),
                baseDate.withHour(12).withMinute(0).withSecond(0).withNano(0),
                JobStatus.SCHEDULED));
    }
}
