package com.serviceforge.persistence;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;
import com.serviceforge.model.Job;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ETSTCC-13074: volatile in-memory implementation (persistence mode "memory"); lost on restart,
 * like jobs and technicians in that mode. Consent is enforced upstream by FeedbackRequest.
 */
@Component
public class InMemoryFeedbackRepository implements FeedbackRepository {

    private final MockDataStore mockDataStore;
    private final Map<Long, Feedback> byJobId = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public InMemoryFeedbackRepository(MockDataStore mockDataStore) {
        this.mockDataStore = mockDataStore;
    }

    @Override
    public Feedback save(Feedback feedback) throws DuplicateFeedbackException {
        Feedback stored = new Feedback(idSequence.getAndIncrement(), feedback.getJobId(), feedback.getRating(),
                feedback.getComment(), feedback.getCreatedAt());
        if (byJobId.putIfAbsent(feedback.getJobId(), stored) != null) {
            throw new DuplicateFeedbackException(feedback.getJobId());
        }
        return stored;
    }

    @Override
    public Optional<Feedback> findByJobId(long jobId) {
        return Optional.ofNullable(byJobId.get(jobId));
    }

    @Override
    public FeedbackStats statsForTechnician(long technicianId) {
        long count = 0;
        long sum = 0;
        for (Job job : mockDataStore.getJobsForTechnician(technicianId)) {
            Feedback f = byJobId.get(job.getId());
            if (f != null) {
                count++;
                sum += f.getRating();
            }
        }
        return new FeedbackStats(count, sum);
    }
}
