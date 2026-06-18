# jEAP Server-Sent Events

jEAP Server-Sent Events is a library for pushing real-time events from a jEAP Spring Boot service to
browser clients over the Server-Sent Events (SSE / `EventSource`) HTTP standard. A service reports
resource changes through a simple business API, and connected UI clients receive a typed event stream
without polling. Multiple backend instances stay in sync through a Kafka topic, so every connected
client receives every relevant event regardless of which instance triggered the change. It provides:

* A `text/event-stream` web endpoint that UI clients subscribe to with the native `EventSource` API
* A business-facing `ResourceMutationService` to publish create / update / delete events
* Kafka fan-out so all backend instances forward events to their own connected clients
* Periodic heartbeats and configurable emitter timeouts to keep connections healthy
* Pluggable authorization: insecure, simple role-based, or semantic resource/operation-based
* Spring Boot auto-configuration enabled by default, switchable via `jeap.sse.enabled`

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                              | File                                                         |
|----------------------------------------------------|--------------------------------------------------------------|
| Getting started (add the dependency, push events)  | [docs/getting-started.md](docs/getting-started.md)           |
| Architecture & event flow                          | [docs/architecture.md](docs/architecture.md)                 |
| Configuration reference (`jeap.sse.*`)             | [docs/configuration.md](docs/configuration.md)               |
| Authorization (insecure / simple / semantic)       | [docs/authorization.md](docs/authorization.md)               |
| Client integration (`EventSource`)                 | [docs/client-integration.md](docs/client-integration.md)     |

## Modules

The artifact most consumers depend on is `jeap-server-sent-events-starter`. The group id for all
modules is `ch.admin.bit.jeap`; the version is managed by this library's parent POM.

| Module                            | Purpose                                                                                  |
|-----------------------------------|------------------------------------------------------------------------------------------|
| `jeap-server-sent-events-core`    | Domain API: `ResourceMutationService`, `ResourceMutationType`, listener interfaces       |
| `jeap-server-sent-events-web`     | SSE web endpoint, heartbeat sender, authorization and Spring Security config             |
| `jeap-server-sent-events-messaging` | Kafka producer/consumer of `NotifyClientCommand` for cross-instance fan-out            |
| `jeap-server-sent-events-starter` | Aggregates `web` + `messaging` and the auto-configuration; the consumer-facing artifact  |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
