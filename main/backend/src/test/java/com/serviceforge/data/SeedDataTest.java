package com.serviceforge.data;

import com.serviceforge.model.Job;
import com.serviceforge.model.JobStatus;
import com.serviceforge.persistence.PersistenceProperties;
import com.serviceforge.persistence.SqliteBootstrapper;
import com.serviceforge.persistence.SqliteConnectionFactory;
import com.serviceforge.persistence.SqliteDataStore;
import com.serviceforge.persistence.SqliteMigrationRunner;
import com.serviceforge.persistence.SqliteSchemaInitializer;
import com.serviceforge.persistence.SqliteSequenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074 / M-01: baseline seed gains exactly one COMPLETED job (id 4, technician 3). */
class SeedDataTest {

    @TempDir
    Path tempDir;

    private static void assertBaselineJobs(List<Job> jobs) {
        assertEquals(4, jobs.size());
        for (int i = 0; i < 4; i++) {
            final long id = i + 1;
            assertTrue(jobs.stream().anyMatch(j -> j.getId() == id), "job id " + id);
        }
        Job j1 = byId(jobs, 1), j2 = byId(jobs, 2), j3 = byId(jobs, 3), j4 = byId(jobs, 4);
        assertEquals(1L, j1.getTechnicianId());
        assertEquals("Acme Corp", j1.getCustomerName());
        assertEquals(1L, j2.getTechnicianId());
        assertEquals("Northwind Traders", j2.getCustomerName());
        assertEquals(2L, j3.getTechnicianId());
        assertEquals("Globex", j3.getCustomerName());
        assertEquals(JobStatus.SCHEDULED, j1.getStatus());
        assertEquals(JobStatus.SCHEDULED, j2.getStatus());
        assertEquals(JobStatus.SCHEDULED, j3.getStatus());
        assertEquals(3L, j4.getTechnicianId());
        assertEquals(JobStatus.COMPLETED, j4.getStatus());
    }

    private static Job byId(List<Job> jobs, long id) {
        return jobs.stream().filter(j -> j.getId() == id).findFirst().orElseThrow();
    }

    @Test
    void mockDataStoreSeedYieldsFourJobsWithCompletedJobFour() {
        MockDataStore store = new MockDataStore();
        store.seed();
        assertBaselineJobs(store.getAllJobs());
        assertEquals(5L, store.nextJobId(), "next booked job gets id 5");
    }

    @Test
    void mockDataStoreSeedDeterministicYieldsFourJobsWithCompletedJobFour() {
        MockDataStore store = new MockDataStore();
        store.seedDeterministic(LocalDateTime.of(2026, 3, 2, 8, 0));
        assertBaselineJobs(store.getAllJobs());
    }

    private SqliteDataStore newSqliteStore(String dbPath) {
        PersistenceProperties props = new PersistenceProperties();
        props.setPersistence("sqlite");
        props.setSqlitePath(dbPath);
        return new SqliteDataStore(new SqliteConnectionFactory(), new SqliteSchemaInitializer(),
                new SqliteSequenceService(), new SqliteBootstrapper(), props, new SqliteMigrationRunner());
    }

    @Test
    void freshSqliteDatabaseSeedsJobFourCompletedForTechnicianThree() {
        SqliteDataStore store = newSqliteStore(tempDir.resolve("seed.db").toString());
        assertBaselineJobs(store.getAllJobs());
    }

    @Test
    void reopeningAPopulatedDatabaseAddsNothing() {
        String path = tempDir.resolve("seed-again.db").toString();
        newSqliteStore(path);
        SqliteDataStore second = newSqliteStore(path);
        assertBaselineJobs(second.getAllJobs());
    }

    @Test
    void existingDatabaseWithoutJobFourDoesNotReceiveIt() throws Exception {
        String path = tempDir.resolve("legacy.db").toString();
        SqliteConnectionFactory factory = new SqliteConnectionFactory();
        try (java.sql.Connection c = factory.open(path)) {
            new SqliteSchemaInitializer().ensureSchema(c);
            try (java.sql.Statement st = c.createStatement()) {
                st.executeUpdate("INSERT INTO technicians(id, name, region) VALUES(1, 'Existing Tech', 'North')");
                st.executeUpdate("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) "
                        + "VALUES(1, 1, 'Existing Customer', '2026-01-01T09:00', '2026-01-01T10:00', 'SCHEDULED')");
            }
        }
        SqliteDataStore store = newSqliteStore(path);
        assertEquals(1, store.getAllJobs().size());
    }
}
