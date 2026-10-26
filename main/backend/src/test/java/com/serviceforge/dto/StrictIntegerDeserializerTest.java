package com.serviceforge.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** ETSTCC-13074: strict integer parsing for jobId (Long) and rating (Integer) (EC-005, EC-010). */
class StrictIntegerDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private FeedbackRequest parse(String json) throws JsonProcessingException {
        return mapper.readValue(json, FeedbackRequest.class);
    }

    @Test
    void acceptsIntegerTokens() throws Exception {
        FeedbackRequest r = parse("{\"jobId\":4,\"rating\":5,\"consent\":true}");
        assertEquals(4L, r.getJobId());
        assertEquals(5, r.getRating());
        assertTrue(r.getConsent());
    }

    @Test
    void acceptsLargeLongJobId() throws Exception {
        assertEquals(9_000_000_000L, parse("{\"jobId\":9000000000,\"rating\":1}").getJobId());
    }

    @Test
    void acceptsLongMaxValue() throws Exception {
        assertEquals(Long.MAX_VALUE, parse("{\"jobId\":9223372036854775807,\"rating\":1}").getJobId());
    }

    @Test
    void rejectsDecimalRating() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":1,\"rating\":3.5}"));
    }

    @Test
    void rejectsDecimalWithZeroFraction() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":1.0,\"rating\":3}"));
    }

    @Test
    void rejectsStringRatingAndJobId() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":1,\"rating\":\"5\"}"));
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":\"1\",\"rating\":5}"));
    }

    @Test
    void rejectsBooleanAndObject() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":true,\"rating\":5}"));
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":1,\"rating\":{}}"));
    }

    @Test
    void rejectsJobIdOverflow() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":9223372036854775808,\"rating\":5}"));
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":99999999999999999999999,\"rating\":5}"));
    }

    @Test
    void rejectsRatingBeyondIntRange() {
        assertThrows(JsonProcessingException.class, () -> parse("{\"jobId\":1,\"rating\":2147483648}"));
    }

    @Test
    void nullTokenYieldsNullField() throws Exception {
        FeedbackRequest r = parse("{\"jobId\":null,\"rating\":null}");
        assertNull(r.getJobId());
        assertNull(r.getRating());
    }

    @Test
    void errorMessageDoesNotEchoTheToken() {
        // Spring builds its message from getOriginalMessage() (no source snippet); our text is fixed.
        JsonMappingException ex = assertThrows(JsonMappingException.class,
                () -> parse("{\"jobId\":\"SENTINEL-TOKEN\",\"rating\":5}"));
        assertFalse(ex.getOriginalMessage().contains("SENTINEL-TOKEN"));
    }
}
