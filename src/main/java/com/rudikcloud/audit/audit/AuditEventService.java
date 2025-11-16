package com.rudikcloud.audit.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuditEventService {

  private final AuditEventRepository repository;
  private final ObjectMapper objectMapper;
  private final EntityManager entityManager;

  public AuditEventService(
      AuditEventRepository repository, ObjectMapper objectMapper, EntityManager entityManager) {
    this.repository = repository;
    this.objectMapper = objectMapper;
    this.entityManager = entityManager;
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

  public AuditEventResponse getById(UUID id) {
    AuditEvent event =
        repository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit event not found"));
    return toResponse(event);
  }

  public List<AuditEventResponse> search(
      String actionType,
      String actorUserId,
      String resourceType,
      String resourceId,
      Instant from,
      Instant to,
      int limit,
      int offset) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AuditEvent> queryDefinition = builder.createQuery(AuditEvent.class);
    Root<AuditEvent> root = queryDefinition.from(AuditEvent.class);

    List<Predicate> filters = new ArrayList<>();
    if (actionType != null && !actionType.isBlank()) {
      filters.add(builder.equal(root.get("actionType"), actionType));
    }
    if (actorUserId != null && !actorUserId.isBlank()) {
      filters.add(builder.equal(root.get("actorUserId"), actorUserId));
    }
    if (resourceType != null && !resourceType.isBlank()) {
      filters.add(builder.equal(root.get("resourceType"), resourceType));
    }
    if (resourceId != null && !resourceId.isBlank()) {
      filters.add(builder.equal(root.get("resourceId"), resourceId));
    }
    if (from != null) {
      filters.add(builder.greaterThanOrEqualTo(root.get("createdAt"), from));
    }
    if (to != null) {
      filters.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
    }

    queryDefinition.select(root);
    if (!filters.isEmpty()) {
      queryDefinition.where(filters.toArray(new Predicate[0]));
    }
    queryDefinition.orderBy(builder.desc(root.get("createdAt")));

    TypedQuery<AuditEvent> typedQuery = entityManager.createQuery(queryDefinition);
    typedQuery.setFirstResult(offset);
    typedQuery.setMaxResults(limit);

    return typedQuery.getResultList().stream().map(this::toResponse).toList();
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
