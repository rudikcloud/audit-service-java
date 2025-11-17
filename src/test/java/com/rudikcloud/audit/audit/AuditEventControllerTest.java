package com.rudikcloud.audit.audit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties = {
      "audit.ingest-token=test-token",
      "spring.datasource.url=jdbc:h2:mem:auditdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.flyway.enabled=false"
    })
@AutoConfigureMockMvc
class AuditEventControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private AuditEventRepository repository;

  @BeforeEach
  void beforeEach() {
    repository.deleteAll();
  }

  @Test
  void postIngestRequiresInternalToken() throws Exception {
    String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "action_type", "FLAG_CREATED",
                "actor_user_id", "user-1",
                "resource_type", "FLAG",
                "created_at", Instant.now().toString()));

    mockMvc
        .perform(post("/audit/events").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void ingestAndSearchWithToken() throws Exception {
    String firstPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "action_type", "FLAG_CREATED",
                "actor_user_id", "user-1",
                "resource_type", "FLAG",
                "resource_id", "newCheckout:dev",
                "created_at", "2026-03-10T10:00:00Z"));

    MvcResult ingestResult =
        mockMvc
            .perform(
                post("/audit/events")
                    .header("X-Internal-Token", "test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(firstPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.action_type").value("FLAG_CREATED"))
            .andReturn();

    Map<?, ?> ingested = objectMapper.readValue(ingestResult.getResponse().getContentAsByteArray(), Map.class);
    String id = String.valueOf(ingested.get("id"));

    String secondPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "action_type", "ORDER_CREATED",
                "actor_user_id", "user-2",
                "resource_type", "ORDER",
                "resource_id", "order-123",
                "created_at", "2026-03-10T11:00:00Z"));

    mockMvc
        .perform(
            post("/audit/events")
                .header("X-Internal-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondPayload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/audit/events")
                .header("X-Internal-Token", "test-token")
                .param("actionType", "FLAG_CREATED")
                .param("resourceType", "FLAG")
                .param("limit", "50")
                .param("offset", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].action_type").value("FLAG_CREATED"))
        .andExpect(jsonPath("$[0].resource_id").value("newCheckout:dev"));

    mockMvc
        .perform(get("/audit/events/{id}", id).header("X-Internal-Token", "test-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.resource_type").value("FLAG"));
  }
}
