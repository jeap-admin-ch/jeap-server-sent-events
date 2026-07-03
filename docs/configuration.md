# Configuration reference

All properties use the prefix `jeap.sse`. The whole library is gated on `jeap.sse.enabled`; when it
is `false` no auto-configuration, endpoint or Kafka listener is created. Defaults marked below come
from `jeap-sse-defaults.properties` in the starter.

## Core

| Name                   | Default | Description                                                                                                        |
|------------------------|---------|--------------------------------------------------------------------------------------------------------------------|
| `jeap.sse.enabled`     | `true`  | Enable or disable the whole SSE support (endpoint, heartbeat and Kafka producer/consumer)                          |
| `jeap.sse.kafka.topic` | —       | Kafka topic the library publishes and consumes on. Convention: `<system>-<applicationname>-notifyclient`. Required |

## Web endpoint

| Name                               | Default              | Description                                                         |
|------------------------------------|----------------------|---------------------------------------------------------------------|
| `jeap.sse.web.endpoint`            | `/ui-api/sse/events` | Path of the SSE (`text/event-stream`) endpoint clients subscribe to |
| `jeap.sse.web.heartbeat.rateInMs`  | `5000`               | Interval in milliseconds between `HEARTBEAT` events                 |
| `jeap.sse.web.emitter.timeoutInMs` | `600000`             | Timeout in milliseconds applied to each `SseEmitter`                |

## Authorization

Exactly one mode must be configured (see [Authorization](authorization.md)). Configuration is
validated at startup by `NotifyClientAuthorizationConfigurationValidator`, which fails fast on
ambiguous or empty combinations.

| Name                            | Default | Description                                                                                                        |
|---------------------------------|---------|--------------------------------------------------------------------------------------------------------------------|
| `jeap.sse.web.insecure.enabled` | `false` | Permit unauthenticated access to the endpoint. Development only; mutually exclusive with the auth properties below |
| `jeap.sse.web.auth.role`        | —       | Simple authorization: role the caller must hold (`ServletSimpleAuthorization`)                                     |
| `jeap.sse.web.auth.resource`    | —       | Semantic authorization: resource the caller must be authorized for (with `auth.operation`)                         |
| `jeap.sse.web.auth.operation`   | —       | Semantic authorization: operation the caller must be authorized for (with `auth.resource`)                         |

Rules enforced by the validator:

- If `insecure.enabled` is `true`, none of `role`, `resource`, `operation` may be set.
- If `insecure.enabled` is `false`, at least one of `role` or (`resource` + `operation`) must be set.
- `resource` and `operation` must be set together; combining them with `role` is rejected.

## Example

```yaml
jeap:
  sse:
    enabled: true
    kafka:
      topic: jme-declaration-service-notifyclient
    web:
      endpoint: /ui-api/sse/events
      heartbeat:
        rateInMs: 5000
      emitter:
        timeoutInMs: 600000
      auth:
        resource: declaration
        operation: read
```

## Related

- [Getting started](getting-started.md)
- [Authorization](authorization.md)
- [Architecture](architecture.md)
- [jeap-server-sent-events](../README.md)
