package com.serviceforge.persistence;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;
import com.serviceforge.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074: in-memory feedback repository (EC-014, volatile by design). */
class InMemoryFeedbackRepositoryTest {

    private MockDataStore store;
    private InMemoryFeedbackRepository repository;

    @BeforeEach
    void setUp() {
        store = new MockDataStore();
        store.seed();
        repository = new InMemoryFeedbackRepository(store);
    }

    private static Feedback feedback(long jobId, int rating) {
        return new Feedback(null, jobId, rating, null, Instant.parse("2026-02-01T10:00:00Z"));
    }

    @Test
    void savesAndFindsByJobId() {
        Feedback saved = repository.save(feedback(1, 4));
        assertNotNull(saved.getId());
        assertEquals(saved.getId(), repository.findByJobId(1).orElseThrow().getId());
        assertTrue(repository.findByJobId(2).isEmpty());
    }

    @Test
    void duplicateForSameJobIsRejected() {
        repository.save(feedback(1, 4));
        assertThrows(DuplicateFeedbackException.class, () -> repository.save(feedback(1, 2)));
        assertEquals(4, repository.findByJobId(1).orElseThrow().getRating());
    }

    @Test
    void idsAreAssignedSequentially() {
        Feedback a = repository.save(feedback(1, 4));
        Feedback b = repository.save(feedback(2, 4));
        assertEquals(a.getId() + 1, b.getId());
    }

    @Test
    void statsAggregateViaMockDataStoreJobs() {
        store.getAllJobs().forEach(j -> j.setStatus(JobStatus.COMPLETED));
        repository.save(feedback(1, 5)); // technician 1
        repository.save(feedback(2, 3)); // technician 1
        repository.save(feedback(3, 1)); // technician 2
        FeedbackStats t1 = repository.statsForTechnician(1);
        assertEquals(2, t1.getCount());
        assertEquals(8, t1.getRatingSum());
        assertEquals(1, repository.statsForTechnician(2).getCount());
        assertEquals(0, repository.statsForTechnician(9).getCount());
    }

    @Test
    void newRepositoryInstanceStartsEmptyVolatile() {
        repository.save(feedback(1, 4));
        InMemoryFeedbackRepository fresh = new InMemoryFeedbackRepository(store);
        assertTrue(fresh.findByJobId(1).isEmpty());
    }
}
