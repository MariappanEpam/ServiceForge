package com.serviceforge.persistence;

import com.serviceforge.ServiceForgeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ETSTCC-13074 (EC-014, M-03 criterion 5): memory persistence mode. The application starts, feedback
 * endpoints work on the volatile in-memory store, and Flyway never touches the SQLite file.
 */
@SpringBootTest(classes = ServiceForgeApplication.class)
@AutoConfigureMockMvc
class FeedbackPersistenceModeTest {

    private static final String DB_PATH = createDbPath();

    private static String createDbPath() {
        try {
            Path dir = Files.createTempDirectory("sf-feedback-mem-");
            dir.toFile().deleteOnExit();
            return dir.resolve("memory-mode.db").toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("serviceforge.persistence", () -> "memory");
        registry.add("serviceforge.sqlite-path", () -> DB_PATH);
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void feedbackWorksInMemoryModeForSeededCompletedJobAndRepeatIsConflict() throws Exception {
        String body = "{\"jobId\":4,\"rating\":5,\"comment\":\"ok\",\"consent\":true}";
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId", is(4)));
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("FEEDBACK_ALREADY_EXISTS")));
        mvc.perform(get("/api/technicians/3/feedback-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackCount", is(1)))
                .andExpect(jsonPath("$.averageRating", is(5.0)));
    }

    @Test
    void scheduledSeedJobIsRejectedInMemoryMode() throws Exception {
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":1,\"rating\":5,\"consent\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("JOB_NOT_COMPLETED")));
    }

    @Test
    void migrationIsSkippedInMemoryMode() throws Exception {
        Set<String> tables = new HashSet<>();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            while (rs.next()) tables.add(rs.getString(1));
        }
        assertFalse(tables.contains("feedback"), "no feedback table: migration must not run in memory mode");
        assertFalse(tables.contains("flyway_schema_history"), "Flyway must not touch the file in memory mode");
    }
}
