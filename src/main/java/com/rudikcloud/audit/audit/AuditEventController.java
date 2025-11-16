package com.rudikcloud.audit.audit;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
