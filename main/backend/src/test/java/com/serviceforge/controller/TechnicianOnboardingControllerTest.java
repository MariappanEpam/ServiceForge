package com.serviceforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceforge.ServiceForgeApplication;
import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ServiceForgeApplication.class)
@AutoConfigureMockMvc
class TechnicianOnboardingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockDataStore mockDataStore;

    @Test
    void createActivateSuspendReactivateOffboardFlow() throws Exception {
        String createBody = "{" +
                "\"name\":\"Alex Chen\"," +
                "\"trade\":\"HVAC\"," +
                "\"region\":\"West\"," +
                "\"email\":\"alex@example.com\"," +
                "\"phone\":\"555-0100\"" +
                "}";

        String createResponse = mvc.perform(post("/api/technicians")
                        .header("X-SF-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technician.id", notNullValue()))
                .andExpect(jsonPath("$.technician.status", is("INVITED")))
                .andExpect(jsonPath("$.invitationToken", not(isEmptyOrNullString())))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(createResponse).get("invitationToken").asText();
        long id = objectMapper.readTree(createResponse).get("technician").get("id").asLong();

        // Token should already be persisted on the created technician by the create endpoint.

        String activateBody = "{" +
                "\"token\":\"" + token + "\"," +
                "\"preferredWorkingHours\":\"9-5\"," +
                "\"emergencyContact\":\"Jamie 555-9999\"" +
                "}";

        mvc.perform(post("/api/technicians/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.onboardingCompletedAt", notNullValue()));

        mvc.perform(post("/api/technicians/" + id + "/suspend")
                        .header("X-SF-Role", "dispatcher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUSPENDED")));

        mvc.perform(post("/api/technicians/" + id + "/reactivate")
                        .header("X-SF-Role", "dispatcher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        mvc.perform(post("/api/technicians/" + id + "/offboard")
                        .header("X-SF-Role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OFFBOARDED")));

        // Booking guard: offboarded technician cannot be booked
        String bookBody = "{" +
                "\"technicianId\":" + id + "," +
                "\"customerName\":\"Blocked\"," +
                "\"startTime\":\"2026-01-01T16:30:00\"," +
                "\"endTime\":\"2026-01-01T17:30:00\"" +
                "}";

        mvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));

        // Invalid transition: cannot reactivate offboarded
        mvc.perform(post("/api/technicians/" + id + "/reactivate")
                        .header("X-SF-Role", "admin"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));

        // Ensure GET detail does not include invitationToken
        mvc.perform(get("/api/technicians/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationToken").doesNotExist());

        // Ensure list filter works
        mvc.perform(get("/api/technicians")
                        .param("status", "OFFBOARDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Ensure search works
        mvc.perform(get("/api/technicians")
                        .param("search", "alex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("alex")));

        // Sanity: datastore reflects offboarded
        Technician stored = mockDataStore.findTechnician(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(TechnicianStatus.OFFBOARDED, stored.getStatus());
    }
}
