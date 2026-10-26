package com.serviceforge.persistence;

import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;

import java.util.Optional;

/** ETSTCC-13074: port for feedback storage (SQLite or in-memory, chosen by PersistenceConfig). */
public interface FeedbackRepository {

    /** Stores the feedback; the id is assigned by the store. One feedback per job. */
    Feedback save(Feedback feedback) throws DuplicateFeedbackException;

    Optional<Feedback> findByJobId(long jobId);

    /** Count and rating sum over feedback of jobs handled by the technician. */
    FeedbackStats statsForTechnician(long technicianId);
}
