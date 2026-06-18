# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the library in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

jEAP Server-Sent Events is a multi-module Maven library that pushes real-time events from a jEAP
Spring Boot service to browser clients over the Server-Sent Events (SSE / `EventSource`) HTTP
standard. The service reports resource mutations through a business API; an SSE web endpoint streams
them to subscribed UI clients. Backend instances are kept in sync through a Kafka topic carrying the
`NotifyClientCommand` Avro command, so every connected client receives every relevant event. Wiring
is provided through Spring Boot auto-configuration, enabled by default and switchable via
`jeap.sse.enabled`.

## Repository layout

```
pom.xml                              # Parent POM (packaging=pom); declares the modules below
jeap-server-sent-events-core/        # Domain API: ResourceMutationService, ResourceMutationType, listener interfaces
jeap-server-sent-events-web/         # SSE endpoint (NotifyClientController), heartbeat, authorization, security config
jeap-server-sent-events-messaging/   # Kafka producer/consumer of NotifyClientCommand for cross-instance fan-out
jeap-server-sent-events-starter/     # Aggregates web + messaging + auto-config; consumer-facing artifact
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

Event flow: business code calls `ResourceMutationService` (core) → `NotifyClientCommandProducer`
(messaging) sends a `NotifyClientCommand` to the Kafka topic → every instance's
`NotifyClientCommandConsumer` consumes it → `ResourceMutationEventHandler` (core) keeps only events
from its own application → `NotifyClientResourceMutationDataSender` (web) writes them to the active
`SseEmitter`s in `NotifyClientController`. See [docs/architecture.md](docs/architecture.md).

## Build & test

```bash
./mvnw -pl <module> -am install      # build a module and its dependencies
./mvnw verify                        # full build incl. tests
./mvnw -pl jeap-server-sent-events-starter test
```

- Parent: `ch.admin.bit.jeap:jeap-internal-spring-boot-parent` (Spring Boot 4 aligned).
- Integration tests extend `KafkaIntegrationTestBase` (from `jeap-messaging-infrastructure-kafka-test`)
  with `@SpringBootTest(webEnvironment = RANDOM_PORT)`; `SSEClientTestSupport` subscribes to the
  endpoint and asserts received event types and payloads.
- Spring Boot 3 maintenance happens on the `release/springboot3` branch; `master` targets Spring Boot 4.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.server.sent.events...`.
- Configuration properties use the prefix `jeap.sse.*`; defaults live in
  `jeap-server-sent-events-starter/.../jeap-sse-defaults.properties`.
- Auto-configuration is registered via `@AutoConfiguration` and
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (one per module:
  `ServerSentEventsAutoConfiguration`, `ServerSentEventsWebAutoConfiguration`,
  `MessagingResourceMutationAutoConfiguration`). All are gated on `jeap.sse.enabled`.
- The Kafka transport uses the `NotifyClientCommand` Avro command (`notify-client-command` message
  type). Both a producer and a consumer message contract are required for the SSE Kafka topic; the
  `NotifyClientContractsValidator` and `NotifyClientTopicValidator` check this at startup.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per
file) and the documentation index in the README.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh <version>` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs, CI will remove it when releasing a version. Do not use the SNAPSHOT
  postfix in other places (CHANGELOG, publiccode.yml etc).
- Keep changelog entries concise and to the point, follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional commits (for
  example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog, and update version/date in `publiccode.yml`.
