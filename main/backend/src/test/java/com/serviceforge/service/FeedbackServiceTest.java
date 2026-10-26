package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
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
import com.serviceforge.model.Technician;
import com.serviceforge.persistence.FeedbackRepository;
import com.serviceforge.persistence.InMemoryDataStoreAdapter;
import com.serviceforge.persistence.InMemoryFeedbackRepository;
import com.serviceforge.persistence.ServiceForgeDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074: FeedbackService rules (AC-010, 016-020, 024; EC-002, 012, 013). */
class FeedbackServiceTest {

    private MockDataStore store;
    private FeedbackRepository repository;
    private FeedbackService service;

    private Long completedJob1;
    private Long completedJob2;
    private Long completedJob3;
    private Long scheduledJob;
    private Long cancelledJob;

    @BeforeEach
    void setUp() {
        store = new MockDataStore();
        store.seed();
        repository = new InMemoryFeedbackRepository(store);
        service = new FeedbackService(new InMemoryDataStoreAdapter(store), repository);

        // Technician 1: two completed jobs, a scheduled and a cancelled one. Technician 2: one completed job.
        completedJob1 = addJob(1L, JobStatus.COMPLETED);
        completedJob2 = addJob(1L, JobStatus.COMPLETED);
        completedJob3 = addJob(2L, JobStatus.COMPLETED);
        scheduledJob = addJob(1L, JobStatus.SCHEDULED);
        cancelledJob = addJob(1L, JobStatus.CANCELLED);
    }

    private Long addJob(Long technicianId, JobStatus status) {
        Long id = store.nextJobId();
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 9, 0).plusDays(id);
        store.save(new Job(id, technicianId, "Test Customer", start, start.plusHours(1), status));
        return id;
    }

    @Test
    void submitStoresFeedbackForCompletedJob() {
        Feedback saved = service.submit(completedJob1, 5, "Great work");
        assertNotNull(saved.getId());
        assertEquals(completedJob1, saved.getJobId());
        assertEquals(5, saved.getRating());
        assertEquals("Great work", saved.getComment());
        assertNotNull(saved.getCreatedAt());
        assertTrue(repository.findByJobId(completedJob1).isPresent());
    }

    @Test
    void seededCompletedJobFourAcceptsFeedback() {
        assertNotNull(service.submit(4L, 4, null).getId());
    }

    @Test
    void commentIsTrimmedAndBlankBecomesNull() {
        assertEquals("padded", service.submit(completedJob1, 4, "   padded \n").getComment());
        assertNull(service.submit(completedJob2, 4, "   \t ").getComment());
    }

    @Test
    void nullCommentIsAllowed() {
        assertNull(service.submit(completedJob1, 3, null).getComment());
    }

    @Test
    void commentOfExactly500CharactersIsAccepted() {
        assertEquals(500, service.submit(completedJob1, 3, "a".repeat(500)).getComment().length());
    }

    @Test
    void commentOf501CharactersIsRejectedAndNothingStored() {
        assertThrows(InvalidFeedbackException.class, () -> service.submit(completedJob1, 3, "a".repeat(501)));
        assertTrue(repository.findByJobId(completedJob1).isEmpty());
    }

    @Test
    void lengthIsCheckedAfterTrim() {
        String padded = "  " + "a".repeat(500) + "  ";
        assertEquals(500, service.submit(completedJob1, 3, padded).getComment().length());
    }

    @Test
    void astralCharactersCountAsTwoUtf16Units() {
        String emoji = new String(Character.toChars(0x1F600)); // 2 UTF-16 units
        assertEquals(500, service.submit(completedJob1, 3, emoji.repeat(250)).getComment().length());
        assertThrows(InvalidFeedbackException.class, () -> service.submit(completedJob2, 3, emoji.repeat(251)));
    }

    @Test
    void invalidFeedbackExceptionDoesNotCarryTheRejectedValue() {
        String sentinel = "SENTINEL-" + "x".repeat(600);
        InvalidFeedbackException ex = assertThrows(InvalidFeedbackException.class,
                () -> service.submit(completedJob1, 3, sentinel));
        assertFalse(ex.getMessage().contains("SENTINEL"));
        assertEquals("comment", ex.getField());
    }

    @Test
    void unknownJobIsRejected() {
        assertThrows(JobNotFoundException.class, () -> service.submit(99999L, 5, null));
    }

    @Test
    void scheduledJobIsRejected() {
        assertThrows(JobNotCompletedException.class, () -> service.submit(scheduledJob, 5, null));
        assertTrue(repository.findByJobId(scheduledJob).isEmpty());
    }

    @Test
    void cancelledJobIsRejected() {
        assertThrows(JobNotCompletedException.class, () -> service.submit(cancelledJob, 5, null));
    }

    @Test
    void secondFeedbackForSameJobIsDuplicateAndOriginalIsKept() {
        service.submit(completedJob1, 5, "first");
        assertThrows(DuplicateFeedbackException.class, () -> service.submit(completedJob1, 1, "second"));
        Feedback kept = repository.findByJobId(completedJob1).orElseThrow();
        assertEquals(5, kept.getRating());
        assertEquals("first", kept.getComment());
    }

    @Test
    void duplicateRaisedByRepositoryRaceIsPropagated() {
        FeedbackRepository racing = new FeedbackRepository() {
            @Override
            public Feedback save(Feedback feedback) {
                throw new DuplicateFeedbackException(feedback.getJobId());
            }

            @Override
            public Optional<Feedback> findByJobId(long jobId) {
                return Optional.empty();
            }

            @Override
            public FeedbackStats statsForTechnician(long technicianId) {
                return new FeedbackStats(0, 0);
            }
        };
        FeedbackService racingService = new FeedbackService(new InMemoryDataStoreAdapter(store), racing);
        assertThrows(DuplicateFeedbackException.class, () -> racingService.submit(completedJob1, 5, null));
    }

    @Test
    void storeIllegalStateIsTranslatedToPersistenceException() {
        ServiceForgeDataStore broken = new InMemoryDataStoreAdapter(store) {
            @Override
            public List<Job> getAllJobs() {
                throw new IllegalStateException("db down");
            }
        };
        FeedbackService brokenService = new FeedbackService(broken, repository);
        assertThrows(FeedbackPersistenceException.class, () -> brokenService.submit(completedJob1, 5, null));
    }

    @Test
    void repositoryIllegalStateIsTranslatedToPersistenceException() {
        FeedbackRepository failing = new FeedbackRepository() {
            @Override
            public Feedback save(Feedback feedback) {
                throw new IllegalStateException("boom");
            }

            @Override
            public Optional<Feedback> findByJobId(long jobId) {
                return Optional.empty();
            }

            @Override
            public FeedbackStats statsForTechnician(long technicianId) {
                return new FeedbackStats(0, 0);
            }
        };
        FeedbackService failingService = new FeedbackService(new InMemoryDataStoreAdapter(store), failing);
        assertThrows(FeedbackPersistenceException.class, () -> failingService.submit(completedJob1, 5, null));
    }

    @Test
    void summaryAveragesFiveAndThreeToFourPointZero() {
        service.submit(completedJob1, 5, null);
        service.submit(completedJob2, 3, null);
        FeedbackSummaryResponse summary = service.summarize(1L);
        assertEquals(1L, summary.getTechnicianId());
        assertEquals(2L, summary.getFeedbackCount());
        assertEquals(new BigDecimal("4.0"), summary.getAverageRating());
    }

    @Test
    void summaryWithoutFeedbackIsNullAndZero() {
        FeedbackSummaryResponse summary = service.summarize(2L);
        assertEquals(0L, summary.getFeedbackCount());
        assertNull(summary.getAverageRating());
    }

    @Test
    void summaryForUnknownTechnicianIsRejected() {
        assertThrows(TechnicianNotFoundException.class, () -> service.summarize(424242L));
    }

    @Test
    void summaryIncludesNewSubmit() {
        service.submit(completedJob1, 5, null);
        assertEquals(new BigDecimal("5.0"), service.summarize(1L).getAverageRating());
        service.submit(completedJob2, 4, null);
        assertEquals(new BigDecimal("4.5"), service.summarize(1L).getAverageRating());
    }

    @Test
    void averageIsRoundedHalfUpToOneDecimal() {
        // (5 + 5 + 4) / 3 = 4.666.. -> 4.7 ; add a second technician-1 job to reach three ratings
        Long extra = addJob(1L, JobStatus.COMPLETED);
        service.submit(completedJob1, 5, null);
        service.submit(completedJob2, 5, null);
        service.submit(extra, 4, null);
        assertEquals(new BigDecimal("4.7"), service.summarize(1L).getAverageRating());
    }

    @Test
    void roundingHalfUpAtExactMidpoint() {
        // 1 + 2 = 3 / 2 = 1.5 exact; 5 ratings (1,1,1,1,2)=6/5=1.2; midpoint case: (3+4+4+4+4+4+4+4)/8 = 3.875 -> 3.9
        FeedbackRepository fixed = new FeedbackRepository() {
            @Override
            public Feedback save(Feedback feedback) {
                return feedback;
            }

            @Override
            public Optional<Feedback> findByJobId(long jobId) {
                return Optional.empty();
            }

            @Override
            public FeedbackStats statsForTechnician(long technicianId) {
                return new FeedbackStats(8, 31);
            }
        };
        FeedbackService s = new FeedbackService(new InMemoryDataStoreAdapter(store), fixed);
        assertEquals(new BigDecimal("3.9"), s.summarize(1L).getAverageRating());
    }

    @Test
    void offboardedAndSuspendedTechniciansStillHaveASummary() {
        Technician offboarded = store.getAllTechnicians().stream()
                .filter(t -> t.getStatus() == com.serviceforge.model.TechnicianStatus.OFFBOARDED).findFirst().orElseThrow();
        Technician suspended = store.getAllTechnicians().stream()
                .filter(t -> t.getStatus() == com.serviceforge.model.TechnicianStatus.SUSPENDED).findFirst().orElseThrow();
        Long job = addJob(offboarded.getId(), JobStatus.COMPLETED);
        service.submit(job, 2, null);
        assertEquals(new BigDecimal("2.0"), service.summarize(offboarded.getId()).getAverageRating());
        assertEquals(0L, service.summarize(suspended.getId()).getFeedbackCount());
    }

    @Test
    void summaryIsPerTechnician() {
        service.submit(completedJob1, 5, null);
        service.submit(completedJob3, 1, null);
        assertEquals(new BigDecimal("5.0"), service.summarize(1L).getAverageRating());
        assertEquals(new BigDecimal("1.0"), service.summarize(2L).getAverageRating());
    }
}
