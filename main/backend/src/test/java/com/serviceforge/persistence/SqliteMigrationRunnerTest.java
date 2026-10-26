package com.serviceforge.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074 Flyway spike: M-03 criteria 1-3 plus schema-diff guarantees (AC-012, AC-022). */
class SqliteMigrationRunnerTest {

    private static final String HISTORY = "flyway_schema_history";

    @TempDir
    Path tempDir;

    private String dbPath;
    private final SqliteConnectionFactory factory = new SqliteConnectionFactory();
    private final SqliteMigrationRunner runner = new SqliteMigrationRunner();

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("test.db").toString();
    }

    private void legacyInit() throws Exception {
        try (Connection c = factory.open(dbPath)) {
            new SqliteSchemaInitializer().ensureSchema(c);
        }
    }

    private TreeSet<String> tables() throws Exception {
        TreeSet<String> out = new TreeSet<>();
        try (Connection c = factory.open(dbPath);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    private List<String> columns(String table) throws Exception {
        List<String> out = new ArrayList<>();
        try (Connection c = factory.open(dbPath);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) out.add(rs.getString("name") + ":" + rs.getString("type"));
        }
        return out;
    }

    private int historyRowsForVersion(String version) throws Exception {
        try (Connection c = factory.open(dbPath);
             PreparedStatement ps = c.prepareStatement("SELECT count(*) FROM " + HISTORY + " WHERE version = ? AND success = 1 AND type = 'SQL'")) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Test
    void freshDatabaseCreatesFeedbackTable() throws Exception {
        legacyInit();
        runner.migrate(dbPath);
        assertTrue(tables().contains("feedback"));
        assertTrue(tables().contains(HISTORY));
        assertEquals(1, historyRowsForVersion("1"));
    }

    @Test
    void legacyOnlyDatabaseAppliesV1Once() throws Exception {
        legacyInit();
        assertFalse(tables().contains(HISTORY));
        runner.migrate(dbPath);
        assertEquals(1, historyRowsForVersion("1"), "V1 must be applied (baseline 0), not skipped");
        assertTrue(tables().contains("feedback"));
        try (Connection c = factory.open(dbPath);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM " + HISTORY + " WHERE type = 'BASELINE' AND version = '0'")) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void rerunIsNoOp() throws Exception {
        legacyInit();
        runner.migrate(dbPath);
        assertDoesNotThrow(() -> runner.migrate(dbPath));
        assertDoesNotThrow(() -> runner.migrate(dbPath));
        assertEquals(1, historyRowsForVersion("1"));
    }

    @Test
    void schemaDiffIsOnlyFeedbackTable() throws Exception {
        legacyInit();
        TreeSet<String> before = tables();
        List<List<String>> columnsBefore = new ArrayList<>();
        for (String t : before) columnsBefore.add(columns(t));

        runner.migrate(dbPath);

        TreeSet<String> after = tables();
        after.remove(HISTORY);
        TreeSet<String> added = new TreeSet<>(after);
        added.removeAll(before);
        assertEquals(new TreeSet<>(List.of("feedback")), added);
        List<List<String>> columnsAfter = new ArrayList<>();
        for (String t : before) columnsAfter.add(columns(t));
        assertEquals(columnsBefore, columnsAfter, "existing tables must be unchanged");
        assertEquals(List.of("id:INTEGER", "job_id:INTEGER", "rating:INTEGER", "comment:TEXT", "created_at:TEXT"), columns("feedback"));
    }

    @Test
    void foreignKeyToJobsIsDeclared() throws Exception {
        legacyInit();
        runner.migrate(dbPath);
        try (Connection c = factory.open(dbPath);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA foreign_key_list(feedback)")) {
            assertTrue(rs.next());
            assertEquals("jobs", rs.getString("table"));
            assertEquals("job_id", rs.getString("from"));
            assertEquals("id", rs.getString("to"));
        }
    }

    @Test
    void persistsAcrossReopen() throws Exception {
        legacyInit();
        runner.migrate(dbPath);
        try (Connection c = factory.open(dbPath); Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES(900, 1, 'Test Customer', '2026-01-01T09:00', '2026-01-01T10:00', 'COMPLETED')");
            st.executeUpdate("INSERT INTO feedback(job_id, rating, comment, created_at) VALUES(900, 4, NULL, '2026-01-01T10:00:00Z')");
        }
        runner.migrate(dbPath);
        try (Connection c = factory.open(dbPath);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT rating FROM feedback WHERE job_id = 900")) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt(1));
        }
    }
}
