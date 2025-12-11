# audit-service-java

`audit-service-java` is the central audit trail service for RudikCloud. It ingests immutable audit events from platform services and provides searchable history APIs.

## What Is an Audit Event

An audit event represents a meaningful state change or security-relevant action (for example `FLAG_CREATED`, `FLAG_UPDATED`, `ORDER_CREATED`, `LOGIN_SUCCESS`).

Core stored fields include:

- `id`
- `org_id`
- `actor_user_id`
- `actor_email`
- `action_type`
- `resource_type`
- `resource_id`
- `before_json`
- `after_json`
- `metadata_json`
- `ip_address`
- `user_agent`
- `created_at`

Schema is created by Flyway migrations at startup.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Service health check |
| `POST` | `/audit/events` | Ingest audit event |
| `GET` | `/audit/events` | Search with filters + pagination |
| `GET` | `/audit/events/{id}` | Fetch single event |

Search filters:

- `actionType`
- `actorUserId`
- `resourceType`
- `resourceId`
- `from`, `to`
- `limit`, `offset`

## Security Model

All `/audit/**` endpoints require header token:

- Header: `X-Internal-Token`
- Value: `AUDIT_INGEST_TOKEN`

Requests without a valid token return `401`.

## Environment Variables

- `DATABASE_URL`: JDBC Postgres URL.
- `AUDIT_INGEST_TOKEN`: Shared internal token.
- `PORT`: API port (default `8000`).
- `OTEL_SERVICE_NAME`: Telemetry service name.
- `OTEL_EXPORTER_OTLP_ENDPOINT`: OTLP traces endpoint.

## Run Locally

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

## Demo

1. In dashboard, create or update a feature flag in `/flags`.
2. Query audit search endpoint for flag events:

```bash
curl -i 'http://127.0.0.1:8004/audit/events?actionType=FLAG_UPDATED&resourceType=FLAG&limit=20&offset=0' \
  -H 'X-Internal-Token: dev-audit-token'
```

3. Open one event by ID:

```bash
curl -i 'http://127.0.0.1:8004/audit/events/<EVENT_ID>' \
  -H 'X-Internal-Token: dev-audit-token'
```

Verify `before_json` and `after_json` reflect the flag change.
