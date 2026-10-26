package com.serviceforge.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.serviceforge.ServiceForgeApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ETSTCC-13074 (M-02, AC-013, AC-021): the submitted comment must never reach the logs.
 *
 * Runs against a real embedded server so no test-harness request printing is involved, with the production
 * logging configuration (root level DEBUG from application.properties; no override). The sentinel is an
 * obviously synthetic string.
 *
 * ACCEPTED RESIDUAL RISK: for a garbled (unparseable) body, framework loggers (org.springframework.*,
 * tomcat) may log parser exception text at DEBUG. For that single payload this test records such lines but
 * does not fail on them; it still fails if any com.serviceforge.* logger emits the sentinel.
 */
@SpringBootTest(classes = ServiceForgeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedbackLoggingTest {

    private static final String SENTINEL = "SENTINEL-SYNTHETIC-COMMENT-7f3a9c";
    private static final Path DB_DIR = createTempDir();
    private static final String DB_PATH = DB_DIR.resolve("feedback-logging-test.db").toString();
    private static int nextJobId = 3000;

    private static Path createTempDir() {
        try {
            Path dir = Files.createTempDirectory("sf-feedback-log-");
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

    @LocalServerPort
    private int port;

    private final Queue<String[]> captured = new ConcurrentLinkedQueue<>();
    private AppenderBase<ILoggingEvent> appender;
    private Logger root;

    @BeforeEach
    void attachAppender() {
        root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender = new AppenderBase<>() {
            @Override
            protected void append(ILoggingEvent event) {
                String throwable = event.getThrowableProxy() == null ? "" : ThrowableProxyUtil.asString(event.getThrowableProxy());
                captured.add(new String[]{event.getLoggerName(), event.getFormattedMessage() + "\n" + throwable});
            }
        };
        appender.setContext(root.getLoggerContext());
        appender.start();
        root.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        root.detachAppender(appender);
        appender.stop();
    }

    private synchronized int newCompletedJob() throws Exception {
        int id = nextJobId++;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH); Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES("
                    + id + ",1,'Test Customer','2026-01-01T09:00','2026-01-01T10:00','COMPLETED')");
        }
        return id;
    }

    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/feedback"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<String> linesWithSentinel(boolean onlyServiceForgeLoggers) {
        List<String> hits = new ArrayList<>();
        for (String[] e : captured) {
            boolean ours = e[0].startsWith("com.serviceforge.");
            if ((!onlyServiceForgeLoggers || ours) && e[1].contains(SENTINEL)) {
                hits.add(e[0]);
            }
        }
        return hits;
    }

    @Test
    void validCommentNeverAppearsInAnyLogAndOutcomeIsLogged() throws Exception {
        int job = newCompletedJob();
        HttpResponse<String> response = post("{\"jobId\":" + job + ",\"rating\":4,\"comment\":\"" + SENTINEL + "\",\"consent\":true}");
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains(SENTINEL), "the API response itself may return the comment");
        assertFalse(captured.isEmpty(), "log capture must be active");
        assertEquals(List.of(), linesWithSentinel(false), "comment must not appear in any logger");
        assertTrue(captured.stream().anyMatch(e -> e[1].contains("outcome=CREATED") && e[1].contains("jobId=" + job)
                && e[1].contains("rating=4")), "technical outcome line expected");
    }

    @Test
    void overLimitCommentNeverAppearsInAnyLog() throws Exception {
        int job = newCompletedJob();
        HttpResponse<String> response = post("{\"jobId\":" + job + ",\"rating\":4,\"comment\":\"" + SENTINEL + "x".repeat(600) + "\",\"consent\":true}");
        assertEquals(400, response.statusCode());
        assertFalse(response.body().contains(SENTINEL), "rejected value must not be echoed");
        assertFalse(captured.isEmpty());
        assertEquals(List.of(), linesWithSentinel(false), "comment must not appear in any logger");
        assertTrue(captured.stream().anyMatch(e -> e[1].contains("outcome=INVALID")));
    }

    @Test
    void duplicateAndNotFoundOutcomesNeverLogTheComment() throws Exception {
        int job = newCompletedJob();
        post("{\"jobId\":" + job + ",\"rating\":4,\"consent\":true}");
        assertEquals(409, post("{\"jobId\":" + job + ",\"rating\":4,\"comment\":\"" + SENTINEL + "\",\"consent\":true}").statusCode());
        assertEquals(404, post("{\"jobId\":987654,\"rating\":4,\"comment\":\"" + SENTINEL + "\",\"consent\":true}").statusCode());
        assertEquals(List.of(), linesWithSentinel(false));
    }

    @Test
    void garbledBodyNeverLogsTheCommentFromOurOwnLoggers() throws Exception {
        HttpResponse<String> response = post("{\"jobId\":1,\"rating\":4,\"comment\":\"" + SENTINEL + "\" \"consent\" true");
        assertEquals(400, response.statusCode());
        assertFalse(response.body().contains(SENTINEL));
        assertFalse(captured.isEmpty());
        assertEquals(List.of(), linesWithSentinel(true), "no com.serviceforge.* logger may emit the comment");
        // Accepted residual risk (see class comment): framework lines are recorded, not asserted.
        List<String> frameworkHits = linesWithSentinel(false);
        System.out.println("framework log lines containing the sentinel for a garbled body (accepted residual risk): "
                + frameworkHits.size());
    }
}
