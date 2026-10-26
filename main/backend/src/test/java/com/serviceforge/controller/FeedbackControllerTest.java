package com.serviceforge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceforge.ServiceForgeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ETSTCC-13074: HTTP contract of the feedback endpoints on a temp SQLite file (never the real dev DB).
 * Covers AC-010, 011, 014, 017, 018, 020, 021; EC-005, 008, 010.
 * Tests assert status and code, not message text, for validation failures (m-05).
 */
@SpringBootTest(classes = ServiceForgeApplication.class)
@AutoConfigureMockMvc
class FeedbackControllerTest {

    private static final Path DB_DIR = createTempDir();
    private static final String DB_PATH = DB_DIR.resolve("feedback-controller-test.db").toString();
    private static final AtomicLong IDS = new AtomicLong(1000);
    private static volatile boolean fixturesApplied;

    private static Path createTempDir() {
        try {
            Path dir = Files.createTempDirectory("sf-feedback-ctrl-");
            dir.toFile().deleteOnExit();
            return dir;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("serviceforge.persistence", () -> "sqlite");
        registry.add("serviceforge.sqlite-path", () -> DB_PATH);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static synchronized void ensureFixtures() throws Exception {
        if (fixturesApplied) return;
        String sql;
        try (InputStream in = FeedbackControllerTest.class.getResourceAsStream("/feedback-test-data.sql")) {
            assertNotNull(in);
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH); Statement st = c.createStatement()) {
            for (String line : sql.split("\\r?\\n")) {
                if (!line.isBlank() && !line.startsWith("--")) st.executeUpdate(line);
            }
        }
        fixturesApplied = true;
    }

    private long newJob(long technicianId, String status) throws Exception {
        ensureFixtures();
        long id = IDS.incrementAndGet();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH); Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES("
                    + id + "," + technicianId + ",'Test Customer','2026-01-01T09:00','2026-01-01T10:00','" + status + "')");
        }
        return id;
    }

    private long newTechnician() throws Exception {
        ensureFixtures();
        long id = IDS.incrementAndGet();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH); Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO technicians(id, name, region) VALUES(" + id + ",'Test Technician','North')");
        }
        return id;
    }

    private ResultActions submit(String body) throws Exception {
        ensureFixtures();
        return mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static String body(Object jobId, Object rating, String commentJson, Object consent) {
        StringBuilder sb = new StringBuilder("{");
        if (jobId != null) sb.append("\"jobId\":").append(jobId).append(',');
        if (rating != null) sb.append("\"rating\":").append(rating).append(',');
        if (commentJson != null) sb.append("\"comment\":").append(commentJson).append(',');
        if (consent != null) sb.append("\"consent\":").append(consent).append(',');
        if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
        return sb.append('}').toString();
    }

    @Test
    void validSubmissionReturns201WithExpectedFieldsOnly() throws Exception {
        long job = newJob(1, "COMPLETED");
        String response = submit(body(job, 5, "\"Great job\"", true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.jobId", is((int) job)))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Great job")))
                .andExpect(jsonPath("$.createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*Z")))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        assertEquals(5, json.size(), "only id, jobId, rating, comment, createdAt");
        assertFalse(response.contains("Test Customer"), "no customer name in response");
        assertFalse(json.has("consent"), "consent is never echoed or stored");
    }

    @Test
    void commentIsOptional() throws Exception {
        long job = newJob(1, "COMPLETED");
        submit(body(job, 3, null, true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment", nullValue()));
    }

    @Test
    void blankCommentIsStoredAsNull() throws Exception {
        long job = newJob(1, "COMPLETED");
        submit(body(job, 3, "\"   \"", true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment", nullValue()));
    }

    @Test
    void commentOf500CharactersIsAccepted() throws Exception {
        long job = newJob(1, "COMPLETED");
        submit(body(job, 3, "\"" + "a".repeat(500) + "\"", true)).andExpect(status().isCreated());
    }

    @Test
    void commentOf501CharactersIsRejectedWithValidationError() throws Exception {
        long job = newJob(1, "COMPLETED");
        submit(body(job, 3, "\"" + "a".repeat(501) + "\"", true))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(content().string(not(containsString("aaaaaaaaaa"))));
    }

    @Test
    void invalidRatingsAreRejectedWith400() throws Exception {
        long job = newJob(1, "COMPLETED");
        for (String rating : new String[]{"0", "6", "-1", "3.5", "\"5\"", "true", "null", "2147483648"}) {
            submit(body(job, rating, null, true)).andExpect(status().isBadRequest());
        }
        submit(body(job, null, null, true)).andExpect(status().isBadRequest());
        submit(body(job, 5, null, true)).andExpect(status().isCreated()); // nothing was stored by the rejects
    }

    @Test
    void invalidJobIdsAreRejectedWith400Never500() throws Exception {
        for (String jobId : new String[]{"1.5", "\"abc\"", "\"1\"", "99999999999999999999", "0", "-1", "null", "true"}) {
            submit(body(jobId, 5, null, true)).andExpect(status().isBadRequest());
        }
        submit(body(null, 5, null, true)).andExpect(status().isBadRequest());
    }

    @Test
    void malformedAndMissingBodiesReturnMalformedRequest() throws Exception {
        ensureFixtures();
        submit("{not json").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
        submit("[1,2,3]").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
    }

    @Test
    void ratingOfWrongTypeReturnsMalformedRequestWithFixedMessage() throws Exception {
        submit("{\"jobId\":1,\"rating\":\"SENTINEL-VALUE\",\"consent\":true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")))
                .andExpect(content().string(not(containsString("SENTINEL-VALUE"))));
    }

    @Test
    void missingOrFalseConsentIsRejectedBeforeAnyJobLookup() throws Exception {
        // Job 987654 does not exist: a 404 would prove the job was looked up before consent was checked.
        submit(body(987654, 5, null, false))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
        submit(body(987654, 5, null, null))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void unknownJobReturns404() throws Exception {
        submit(body(987654, 5, null, true))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code", is("JOB_NOT_FOUND")));
    }

    @Test
    void scheduledAndCancelledJobsReturnJobNotCompleted() throws Exception {
        long scheduled = newJob(1, "SCHEDULED");
        long cancelled = newJob(1, "CANCELLED");
        submit(body(scheduled, 5, null, true))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("JOB_NOT_COMPLETED")));
        submit(body(cancelled, 5, null, true))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("JOB_NOT_COMPLETED")));
    }

    @Test
    void secondFeedbackForSameJobReturns409AndOriginalIsKept() throws Exception {
        long job = newJob(1, "COMPLETED");
        submit(body(job, 5, "\"first\"", true)).andExpect(status().isCreated());
        submit(body(job, 1, "\"second\"", true))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code", is("FEEDBACK_ALREADY_EXISTS")));
        long tech = newTechnician();
        long other = newJob(tech, "COMPLETED");
        submit(body(other, 5, null, true)).andExpect(status().isCreated());
        mvc.perform(get("/api/technicians/" + tech + "/feedback-summary"))
                .andExpect(jsonPath("$.feedbackCount", is(1)));
    }

    @Test
    void errorBodiesUseTheStandardApiErrorShapeAndEchoRequestId() throws Exception {
        ensureFixtures();
        mvc.perform(post("/api/feedback").header("X-Request-Id", "req-123")
                        .contentType(MediaType.APPLICATION_JSON).content(body(987654, 5, null, true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.requestId", is("req-123")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.userMessage", notNullValue()));
    }

    @Test
    void summaryAveragesFiveAndThree() throws Exception {
        long tech = newTechnician();
        long a = newJob(tech, "COMPLETED");
        long b = newJob(tech, "COMPLETED");
        submit(body(a, 5, null, true)).andExpect(status().isCreated());
        submit(body(b, 3, null, true)).andExpect(status().isCreated());
        String response = mvc.perform(get("/api/technicians/" + tech + "/feedback-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianId", is((int) tech)))
                .andExpect(jsonPath("$.averageRating", is(4.0)))
                .andExpect(jsonPath("$.feedbackCount", is(2)))
                .andReturn().getResponse().getContentAsString();
        assertEquals(3, objectMapper.readTree(response).size(), "no comments, names or other fields");
    }

    @Test
    void summaryWithoutFeedbackHasNullAverageAndZeroCount() throws Exception {
        long tech = newTechnician();
        mvc.perform(get("/api/technicians/" + tech + "/feedback-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating", nullValue()))
                .andExpect(jsonPath("$.feedbackCount", is(0)));
    }

    @Test
    void summaryReflectsANewSubmit() throws Exception {
        long tech = newTechnician();
        long job = newJob(tech, "COMPLETED");
        mvc.perform(get("/api/technicians/" + tech + "/feedback-summary"))
                .andExpect(jsonPath("$.feedbackCount", is(0)));
        submit(body(job, 2, null, true)).andExpect(status().isCreated());
        mvc.perform(get("/api/technicians/" + tech + "/feedback-summary"))
                .andExpect(jsonPath("$.feedbackCount", is(1)))
                .andExpect(jsonPath("$.averageRating", is(2.0)));
    }

    @Test
    void summaryForUnknownTechnicianReturns404() throws Exception {
        mvc.perform(get("/api/technicians/424242/feedback-summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("TECHNICIAN_NOT_FOUND")));
    }

    @Test
    void summaryWithNonNumericIdReturns400() throws Exception {
        mvc.perform(get("/api/technicians/abc/feedback-summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }

    @Test
    void existingEndpointsStillWork() throws Exception {
        ensureFixtures();
        mvc.perform(get("/api/technicians")).andExpect(status().isOk());
        mvc.perform(get("/api/technicians/1/jobs")).andExpect(status().isOk());
        // Legacy behaviour is unchanged: our advice is scoped to FeedbackController, so the existing catch-all still applies here.
        mvc.perform(get("/api/technicians/abc")).andExpect(status().is5xxServerError());
    }
}
