package com.serviceforge.service;

import com.serviceforge.dto.FeedbackSummaryResponse;
import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.exception.FeedbackPersistenceException;
import com.serviceforge.exception.InvalidFeedbackException;
import com.serviceforge.exception.JobNotCompletedException;
import com.serviceforge.exception.JobNotFoundException;
import com.serviceforge.exception.TechnicianNotFoundException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;
import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.persistence.FeedbackRepository;
import com.serviceforge.persistence.ServiceForgeDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * ETSTCC-13074: submit feedback for a completed job and summarise a technician's rating.
 * Privacy: logs only outcome code, ids and rating. It never logs the comment, request objects,
 * customer names or raw exception messages. Consent (consent=true) is enforced by FeedbackRequest
 * before this layer is reached and is not stored.
 */
@Service
public class FeedbackService {

    static final int MAX_COMMENT_LENGTH = 500; // UTF-16 code units, checked after trimming

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final ServiceForgeDataStore dataStore;
    private final FeedbackRepository repository;

    public FeedbackService(ServiceForgeDataStore dataStore, FeedbackRepository repository) {
        this.dataStore = dataStore;
        this.repository = repository;
    }

    /**
     * Order: comment normalise + length, job exists, job COMPLETED, duplicate pre-check, save.
     * Nothing is stored on any failure.
     */
    public Feedback submit(Long jobId, Integer rating, String comment) {
        String normalized = normalizeComment(comment, jobId);

        Job job = findJob(jobId);
        if (job == null) {
            log.info("feedback outcome=JOB_NOT_FOUND jobId={}", jobId);
            throw new JobNotFoundException(jobId);
        }
        if (job.getStatus() != JobStatus.COMPLETED) {
            log.info("feedback outcome=JOB_NOT_COMPLETED jobId={}", jobId);
            throw new JobNotCompletedException(jobId);
        }
        if (repository.findByJobId(jobId).isPresent()) {
            log.info("feedback outcome=DUPLICATE jobId={}", jobId);
            throw new DuplicateFeedbackException(jobId);
        }

        Feedback saved;
        try {
            saved = repository.save(new Feedback(null, jobId, rating, normalized, Instant.now()));
        } catch (DuplicateFeedbackException e) {
            log.info("feedback outcome=DUPLICATE jobId={}", jobId);
            throw e;
        } catch (IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to store feedback", e);
        }
        log.info("feedback outcome=CREATED feedbackId={} jobId={} rating={}", saved.getId(), jobId, rating);
        return saved;
    }

    public FeedbackSummaryResponse summarize(Long technicianId) {
        try {
            if (dataStore.findTechnician(technicianId).isEmpty()) {
                throw new TechnicianNotFoundException(technicianId);
            }
        } catch (IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to read technician", e);
        }
        FeedbackStats stats = repository.statsForTechnician(technicianId);

        FeedbackSummaryResponse response = new FeedbackSummaryResponse();
        response.setTechnicianId(technicianId);
        response.setFeedbackCount(stats.getCount());
        if (stats.getCount() == 0) {
            response.setAverageRating(null);
        } else {
            response.setAverageRating(BigDecimal.valueOf(stats.getRatingSum())
                    .divide(BigDecimal.valueOf(stats.getCount()), 1, RoundingMode.HALF_UP));
        }
        return response;
    }

    private String normalizeComment(String comment, Long jobId) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_COMMENT_LENGTH) {
            log.info("feedback outcome=INVALID jobId={}", jobId);
            throw new InvalidFeedbackException("comment", "comment must be at most " + MAX_COMMENT_LENGTH + " characters");
        }
        return trimmed;
    }

    private Job findJob(Long jobId) {
        try {
            for (Job job : dataStore.getAllJobs()) {
                if (jobId.equals(job.getId())) {
                    return job;
                }
            }
            return null;
        } catch (IllegalStateException e) {
            throw new FeedbackPersistenceException("Failed to read job", e);
        }
    }
}
