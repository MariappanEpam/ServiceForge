package com.serviceforge.persistence;

import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.exception.FeedbackPersistenceException;
import com.serviceforge.model.Feedback;
import com.serviceforge.model.FeedbackStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074: SQLite feedback repository on a temp file DB (AC-012, AC-016, AC-019; EC-011). */
class SqliteFeedbackRepositoryTest {

    @TempDir
    Path tempDir;

    private SqliteFeedbackRepository repository;
    private String dbPath;

    @BeforeEach
    void setUp() throws Exception {
        dbPath = tempDir.resolve("feedback-test.db").toString();
        SqliteConnectionFactory factory = new SqliteConnectionFactory();
        try (Connection c = factory.open(dbPath)) {
            new SqliteSchemaInitializer().ensureSchema(c);
        }
        new SqliteMigrationRunner().migrate(dbPath);
        applyFixtures(factory);
        PersistenceProperties props = new PersistenceProperties();
        props.setPersistence("sqlite");
        props.setSqlitePath(dbPath);
        repository = new SqliteFeedbackRepository(factory, props);
    }

    private void applyFixtures(SqliteConnectionFactory factory) throws Exception {
        String sql;
        try (InputStream in = getClass().getResourceAsStream("/feedback-test-data.sql")) {
            assertNotNull(in);
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection c = factory.open(dbPath); Statement st = c.createStatement()) {
            for (String line : sql.split("\\r?\\n")) {
                if (!line.isBlank() && !line.startsWith("--")) {
                    st.executeUpdate(line);
                }
            }
        }
    }

    private static Feedback feedback(long jobId, int rating, String comment) {
        return new Feedback(null, jobId, rating, comment, Instant.parse("2026-02-01T10:00:00Z"));
    }

    @Test
    void insertsAndFindsFeedbackByJobId() {
        Feedback saved = repository.save(feedback(101, 5, "Great"));
        assertNotNull(saved.getId());
        Feedback found = repository.findByJobId(101).orElseThrow();
        assertEquals(saved.getId(), found.getId());
        assertEquals(5, found.getRating());
        assertEquals("Great", found.getComment());
        assertEquals(Instant.parse("2026-02-01T10:00:00Z"), found.getCreatedAt());
    }

    @Test
    void storesNullComment() {
        repository.save(feedback(101, 3, null));
        assertNull(repository.findByJobId(101).orElseThrow().getComment());
    }

    @Test
    void findByJobIdIsEmptyWhenNoFeedback() {
        assertTrue(repository.findByJobId(102).isEmpty());
    }

    @Test
    void secondFeedbackForSameJobIsDuplicate() {
        repository.save(feedback(101, 5, null));
        assertThrows(DuplicateFeedbackException.class, () -> repository.save(feedback(101, 1, "again")));
        assertEquals(5, repository.findByJobId(101).orElseThrow().getRating(), "original row unchanged");
    }

    @Test
    void foreignKeyToJobsIsEnforced() {
        assertThrows(FeedbackPersistenceException.class, () -> repository.save(feedback(9999, 4, null)));
    }

    @Test
    void checkConstraintRejectsOutOfRangeRatingAsPersistenceFailure() {
        assertThrows(FeedbackPersistenceException.class, () -> repository.save(feedback(101, 6, null)));
    }

    @Test
    void persistenceFailureMessageDoesNotContainComment() {
        String sentinel = "SENTINEL-COMMENT-TEXT";
        FeedbackPersistenceException ex = assertThrows(FeedbackPersistenceException.class,
                () -> repository.save(feedback(9999, 4, sentinel)));
        assertFalse(ex.getMessage().contains(sentinel));
    }

    @Test
    void statsForTechnicianAggregatesOnlyThatTechniciansJobs() {
        repository.save(feedback(101, 5, null));
        repository.save(feedback(102, 3, null));
        repository.save(feedback(103, 1, null));
        FeedbackStats t1 = repository.statsForTechnician(1);
        assertEquals(2, t1.getCount());
        assertEquals(8, t1.getRatingSum());
        FeedbackStats t2 = repository.statsForTechnician(2);
        assertEquals(1, t2.getCount());
        assertEquals(1, t2.getRatingSum());
    }

    @Test
    void statsForTechnicianWithoutFeedbackIsZero() {
        FeedbackStats stats = repository.statsForTechnician(3);
        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getRatingSum());
    }

    @Test
    void concurrentDoubleInsertGivesExactlyOneSuccess() throws Exception {
        int threads = 4;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int rating = 1 + (i % 5);
            Callable<Boolean> task = () -> {
                start.await();
                try {
                    repository.save(feedback(101, rating, null));
                    return true;
                } catch (DuplicateFeedbackException e) {
                    return false;
                }
            };
            results.add(pool.submit(task));
        }
        start.countDown();
        int successes = 0;
        for (Future<Boolean> f : results) {
            if (f.get()) successes++;
        }
        pool.shutdown();
        assertEquals(1, successes);
        assertEquals(1, repository.statsForTechnician(1).getCount());
    }
}
