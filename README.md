# audit-service-java

Spring Boot service for RudikCloud Milestone 5 audit ingestion and search.

## Current scaffold

- Java 21 + Spring Boot
- `GET /health`
- Dockerfile for containerized run

## Run in Docker

```bash
docker build -t audit-service-java .
docker run --rm -p 8004:8000 \
  -e DATABASE_URL='jdbc:postgresql://host.docker.internal:5432/rudikcloud?user=rudik&password=rudik' \
  -e AUDIT_INGEST_TOKEN='dev-audit-token' \
  audit-service-java
```
