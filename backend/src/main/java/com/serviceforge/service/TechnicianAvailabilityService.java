package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.model.Technician;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Feature 1 — Technician Availability Calendar.
 *
 * See pipeline/features/feature-1-technician-availability.md for the spec this was built against,
 * and pipeline/decisions/feature-1-decisions.md for the decisions referenced below.
 */
@Service
public class TechnicianAvailabilityService implements IReservationService{

    /**
     * Decision (see pipeline/decisions/feature-1-decisions.md): every booked job reserves an
     * additional 45-minute travel buffer on top of its own start/end time, to account for the
     * technician getting to the next job. This is what actually limits how many jobs can be
     * scheduled for one technician in a day — anything reasoning about daily capacity needs
     * this number, not a guess.
     */
    public static final int TRAVEL_BUFFER_MINUTES = 45;

    private final MockDataStore dataStore;

    public TechnicianAvailabilityService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Technician> getAllTechnicians() {
        return dataStore.getAllTechnicians();
    }

    public Optional<Technician> findTechnician(Long technicianId) {
        return dataStore.findTechnician(technicianId);
    }

    public List<Job> getJobsForTechnician(Long technicianId) {
        return dataStore.getJobsForTechnician(technicianId);
    }

    /**
     * Books a new job for a technician.
     *
     * KNOWN ISSUE (see pipeline/features/feature-1-technician-availability.md — "Known issue"):
     * this method is supposed to reject a booking that overlaps an existing one for the same
     * technician, but the check below only compares exact start times. Two jobs with different
     * but overlapping start times are currently accepted silently instead of being rejected with
     * a conflict error. Fixing this should not remove or ignore the travel-buffer decision above.
     */
    @Override
    public Job bookJob(Long technicianId, String customerName, LocalDateTime startTime, LocalDateTime endTime) {
        Technician technician = dataStore.findTechnician(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("No technician with id " + technicianId));

        List<Job> existingJobs = dataStore.getJobsForTechnician(technicianId);

        // Validate times
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // New occupied window includes travel buffer after the job end, per decision/rule.
        LocalDateTime newOccupiedStart = startTime;
        LocalDateTime newOccupiedEndWithBuffer = endTime.plusMinutes(TRAVEL_BUFFER_MINUTES);

        boolean hasConflict = existingJobs.stream().anyMatch(existing -> {
            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEndWithBuffer = existing.getEndTime().plusMinutes(TRAVEL_BUFFER_MINUTES);

            // Overlap check: if new start is before existing end-with-buffer AND new end is after existing start
            // we consider that a conflict. Equality at the boundary is allowed (one job ending exactly when
            // the next occupied window starts).
            return newOccupiedStart.isBefore(existingEndWithBuffer) && endTime.isAfter(existingStart);
        });

        if (hasConflict) {
            // Follow the rule's prescribed rejection message
            throw new IllegalStateException("Technician is unavailable during the requested time slot. Select another technician or choose a different time.");
        }

        Job job = new Job(dataStore.nextJobId(), technicianId, customerName, startTime, endTime, JobStatus.SCHEDULED);
        return dataStore.save(job);
    }
}
