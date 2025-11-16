package com.rudikcloud.audit.audit;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/events")
public class AuditEventController {

  private final AuditEventService auditEventService;

  public AuditEventController(AuditEventService auditEventService) {
    this.auditEventService = auditEventService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuditEventResponse ingest(@Valid @RequestBody AuditEventIngestRequest request) {
    return auditEventService.create(request);
  }

  @GetMapping
  public List<AuditEventResponse> search(
      @RequestParam(required = false) String actionType,
      @RequestParam(required = false) String actorUserId,
      @RequestParam(required = false) String resourceType,
      @RequestParam(required = false) String resourceId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    int normalizedLimit = Math.min(Math.max(limit, 1), 200);
    int normalizedOffset = Math.max(offset, 0);
    return auditEventService.search(
        actionType,
        actorUserId,
        resourceType,
        resourceId,
        from,
        to,
        normalizedLimit,
        normalizedOffset);
  }

  @GetMapping("/{id}")
  public AuditEventResponse getById(@PathVariable UUID id) {
    return auditEventService.getById(id);
  }
}
