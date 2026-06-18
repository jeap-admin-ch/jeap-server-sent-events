# Getting started

This page shows how to add jEAP Server-Sent Events to a Spring Boot service and push a resource-change
event to subscribed browser clients. For the bigger picture see [Architecture](architecture.md).

## 1. Add the dependency

The starter aggregates the web endpoint, the Kafka messaging and the Spring Boot auto-configuration:

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-server-sent-events-starter</artifactId>
</dependency>
```

The version is managed by this library. The service must already have a working
[jEAP Messaging](https://github.com/jeap-admin-ch/jeap-messaging) Kafka setup (cluster, schema
registry, `systemName`), as the library publishes and consumes a `NotifyClientCommand` over Kafka.

## 2. Configure the Kafka topic and authorization

The minimum configuration points the library at a Kafka topic and chooses an authorization mode.
All properties live under `jeap.sse.*` (see the full [Configuration reference](configuration.md)).

```yaml
jeap:
  sse:
    kafka:
      topic: my-system-my-app-notifyclient
    web:
      insecure:
        enabled: true   # development only; pick a real mode for production
```

The topic name should follow the convention `<system>-<applicationname>-notifyclient`. Exactly one
authorization mode must be configured — see [Authorization](authorization.md).

## 3. Declare the message contracts

The SSE Kafka topic carries the `NotifyClientCommand`. The service must declare **both** a producer
and a consumer contract for it (every instance both publishes and consumes), typically on the
`@SpringBootApplication` class. The startup validators fail fast if a contract is missing.

```java
@SpringBootApplication
@JeapMessageProducerContract(value = NotifyClientCommand.TypeRef.class, topic = "my-system-my-app-notifyclient")
@JeapMessageConsumerContract(value = NotifyClientCommand.TypeRef.class, topic = "my-system-my-app-notifyclient")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 4. Publish a resource-change event

Inject `ResourceMutationService` and call it from business logic whenever a resource changes. The
`resourcePath` is a reference the UI can use to fetch the actual data with a normal REST call.

```java
@Service
@RequiredArgsConstructor
class DeclarationService {
    private final ResourceMutationService resourceMutationService;

    void createDeclaration(String declarationId) {
        // ... persist the declaration ...
        resourceMutationService.resourceMutation(
                ResourceMutationType.RESOURCE_CREATED,
                "/declarations/" + declarationId);
    }
}
```

The event is sent to the Kafka topic, fanned out to every backend instance, and pushed to all
subscribed clients as an SSE event named `RESOURCE_CREATED` with a JSON payload `{"path": "..."}`.

## 5. Subscribe from the browser

UI clients connect to the SSE endpoint (default `/ui-api/sse/events`) with the native `EventSource`
API. See [Client integration](client-integration.md).

```javascript
const source = new EventSource('/ui-api/sse/events');
source.addEventListener('RESOURCE_CREATED', e => {
    const { path } = JSON.parse(e.data);
    // ... reload the resource at `path` via REST ...
});
```

## Related

- [Architecture](architecture.md)
- [Configuration reference](configuration.md)
- [Authorization](authorization.md)
- [Client integration](client-integration.md)
- [jeap-server-sent-events](../README.md)
