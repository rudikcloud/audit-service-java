# audit-service-java

Spring Boot service for RudikCloud Milestone 5 audit ingestion and search.

## Features

- Java 21 + Spring Boot
- Flyway migration for `audit_events` table and indexes
- `GET /health`
- `POST /audit/events` (ingestion, token-protected)
- `GET /audit/events` (search, token-protected)
- `GET /audit/events/{id}` (detail, token-protected)

## Environment variables

- `DATABASE_URL`: JDBC URL for Postgres.
  - Example: `jdbc:postgresql://postgres:5432/rudikcloud?user=rudik&password=rudik`
- `AUDIT_INGEST_TOKEN`: shared internal token required via `X-Internal-Token` for `/audit/**`.
- `PORT`: server port (default `8000`).
- `OTEL_SERVICE_NAME`: service name for tracing (default `audit-service-java`).
- `OTEL_EXPORTER_OTLP_ENDPOINT`: OTLP traces endpoint used by Spring Boot tracing.
  - Local host example: `http://localhost:4318/v1/traces`
  - Docker Compose example: `http://otel-collector:4318/v1/traces`

## Run locally

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  maven:3.9.9-eclipse-temurin-21 mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-DPORT=8000 -DDATABASE_URL=jdbc:postgresql://localhost:5432/rudikcloud?user=rudik&password=rudik -DAUDIT_INGEST_TOKEN=dev-audit-token"
```

## Run in Docker

```bash
docker build -t audit-service-java .
docker run --rm -p 8004:8000 \
  -e DATABASE_URL='jdbc:postgresql://host.docker.internal:5432/rudikcloud?user=rudik&password=rudik' \
  -e AUDIT_INGEST_TOKEN='dev-audit-token' \
  audit-service-java
```

## API examples

```bash
# health
curl -i http://127.0.0.1:8004/health

# ingest event (requires token)
curl -i -X POST http://127.0.0.1:8004/audit/events \
  -H 'Content-Type: application/json' \
  -H 'X-Internal-Token: dev-audit-token' \
  -d '{
    "action_type": "FLAG_CREATED",
    "actor_user_id": "user-1",
    "actor_email": "user-1@example.com",
    "resource_type": "FLAG",
    "resource_id": "newCheckout:dev",
    "before_json": null,
    "after_json": {"enabled": true, "rollout_percent": 100},
    "metadata_json": {"source": "flags-service"},
    "created_at": "2026-03-10T12:00:00Z"
  }'

# search by action + resource type
curl -i 'http://127.0.0.1:8004/audit/events?actionType=FLAG_CREATED&resourceType=FLAG&limit=50&offset=0' \
  -H 'X-Internal-Token: dev-audit-token'
```

## Notes

- Flyway runs automatically at startup (`V1__create_audit_events.sql`).
- Search supports filters:
  - `actionType`, `actorUserId`, `resourceType`, `resourceId`, `from`, `to`, `limit`, `offset`.

## Observability verification

With infra observability enabled:

1. Call `POST /audit/events` and `GET /audit/events`.
2. Open Grafana (`http://localhost:3001`) and Explore with the Tempo datasource.
3. Search traces with `service.name=audit-service-java`.
