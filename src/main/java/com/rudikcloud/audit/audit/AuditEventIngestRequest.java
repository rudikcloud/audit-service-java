package com.rudikcloud.audit.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class AuditEventIngestRequest {

  private UUID id;
  private String orgId;

  @NotBlank
  private String actorUserId;

  private String actorEmail;

  @NotBlank
  private String actionType;

  @NotBlank
  private String resourceType;

  private String resourceId;
  private JsonNode beforeJson;
  private JsonNode afterJson;
  private JsonNode metadataJson;
  private String ipAddress;
  private String userAgent;

  @NotNull
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getActorUserId() {
    return actorUserId;
  }

  public void setActorUserId(String actorUserId) {
    this.actorUserId = actorUserId;
  }

  public String getActorEmail() {
    return actorEmail;
  }

  public void setActorEmail(String actorEmail) {
    this.actorEmail = actorEmail;
  }

  public String getActionType() {
    return actionType;
  }

  public void setActionType(String actionType) {
    this.actionType = actionType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public JsonNode getBeforeJson() {
    return beforeJson;
  }

  public void setBeforeJson(JsonNode beforeJson) {
    this.beforeJson = beforeJson;
  }

  public JsonNode getAfterJson() {
    return afterJson;
  }

  public void setAfterJson(JsonNode afterJson) {
    this.afterJson = afterJson;
  }

  public JsonNode getMetadataJson() {
    return metadataJson;
  }

  public void setMetadataJson(JsonNode metadataJson) {
    this.metadataJson = metadataJson;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
