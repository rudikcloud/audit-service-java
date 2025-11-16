package com.rudikcloud.audit.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {

  private final AuditEventRepository repository;
  private final ObjectMapper objectMapper;

  public AuditEventService(AuditEventRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  public AuditEventResponse create(AuditEventIngestRequest request) {
    AuditEvent event = new AuditEvent();
    event.setId(request.getId() != null ? request.getId() : UUID.randomUUID());
    event.setOrgId(request.getOrgId());
    event.setActorUserId(request.getActorUserId());
    event.setActorEmail(request.getActorEmail());
    event.setActionType(request.getActionType());
    event.setResourceType(request.getResourceType());
    event.setResourceId(request.getResourceId());
    event.setBeforeJson(writeJson(request.getBeforeJson()));
    event.setAfterJson(writeJson(request.getAfterJson()));
    event.setMetadataJson(writeJson(request.getMetadataJson()));
    event.setIpAddress(request.getIpAddress());
    event.setUserAgent(request.getUserAgent());
    event.setCreatedAt(request.getCreatedAt());

    AuditEvent saved = repository.save(event);
    return toResponse(saved);
  }

  AuditEventResponse toResponse(AuditEvent event) {
    return new AuditEventResponse(
        event.getId(),
        event.getOrgId(),
        event.getActorUserId(),
        event.getActorEmail(),
        event.getActionType(),
        event.getResourceType(),
        event.getResourceId(),
        readJson(event.getBeforeJson()),
        readJson(event.getAfterJson()),
        readJson(event.getMetadataJson()),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getCreatedAt());
  }

  private String writeJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(node);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid JSON payload", ex);
    }
  }

  private JsonNode readJson(String content) {
    if (content == null || content.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readTree(content);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Invalid stored JSON", ex);
    }
  }
}
