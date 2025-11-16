package com.rudikcloud.audit.audit;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    String orgId,
    String actorUserId,
    String actorEmail,
    String actionType,
    String resourceType,
    String resourceId,
    JsonNode beforeJson,
    JsonNode afterJson,
    JsonNode metadataJson,
    String ipAddress,
    String userAgent,
    Instant createdAt) {}
