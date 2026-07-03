# Architecture

jEAP Server-Sent Events pushes resource-change notifications from a service to its browser clients.
The challenge it solves is **horizontal scaling**: an `EventSource` connection is bound to a single
backend instance, but a change may be triggered on any instance. The library therefore routes every
change through a Kafka topic so all instances forward it to their own connected clients.

## Modules and responsibilities

| Module      | Key types                                                                                                                       |
|-------------|---------------------------------------------------------------------------------------------------------------------------------|
| `core`      | `ResourceMutationService`, `ResourceMutationType`, `ResourceMutationEventHandler`, listener interfaces                          |
| `messaging` | `NotifyClientCommandProducer`, `NotifyClientCommandConsumer`, contract & topic validators                                       |
| `web`       | `NotifyClientController` (SSE endpoint), `NotifyClientResourceMutationDataSender`, `NotifyClientHeartbeatSender`, authorization |
| `starter`   | Aggregates the above plus `ServerSentEventsAutoConfiguration`                                                                   |

## Event flow

A resource mutation travels from business code through Kafka back into every instance's SSE emitters:

```mermaid
flowchart LR
    BL[Business code] -->|resourceMutation| RMS[ResourceMutationService]
    RMS -->|onResourceMutation| P[NotifyClientCommandProducer]
    P -->|NotifyClientCommand| K[(Kafka topic)]
    K --> C1[NotifyClientCommandConsumer<br/>instance A]
    K --> C2[NotifyClientCommandConsumer<br/>instance B]
    C1 --> H1[ResourceMutationEventHandler]
    H1 -->|same application?| S1[DataSender → SseEmitters]
    S1 --> UIA[Browser clients on A]
    C2 --> H2[ResourceMutationEventHandler]
    H2 -->|same application?| S2[DataSender → SseEmitters]
    S2 --> UIB[Browser clients on B]
```

Step by step:

1. **Publish.** Business code calls `ResourceMutationService.resourceMutation(type, resourcePath)`.
   The service fans out to all registered `ResourceMutationListener`s; in a configured service the
   only listener is `NotifyClientCommandProducer`.
2. **To Kafka.** The producer builds a `NotifyClientCommand` (Avro) tagged with the sending
   application name and the `resourcePath`, and sends it **synchronously** to `jeap.sse.kafka.topic`.
3. **Consume on every instance.** Each instance runs a `NotifyClientCommandConsumer` with a unique
   listener id (`${spring.application.name}-${random.uuid}`), so all instances receive the command.
4. **Filter.** `ResourceMutationEventHandler` drops commands whose `sendingApplication` differs from
   the instance's own `spring.application.name` — only an application notifies its own clients.
5. **Push.** `NotifyClientResourceMutationDataSender` (a `ResourceMutationEventListener`) serializes
   `{"path": resourcePath}` to JSON and calls `NotifyClientController.sendEvent(type, data)`, which
   writes the event to every active `SseEmitter`.

## Multi-instance support

A service usually runs as several instances behind a load balancer. Each browser client holds its
`EventSource` connection to exactly one of them, while the mutation it should be notified about may
be processed on any other. The diagram below shows how the Kafka topic bridges this gap: UI 1 is
connected to instance 1, UI 2 to instance 2, and a resource deletion handled by instance 2 still
reaches both clients.

Every instance subscribes to the topic with its **own consumer group** (the listener id is
`${spring.application.name}-${random.uuid}`), so the `NotifyClientCommand` is not load-balanced
across instances but delivered to *all* of them. Each instance then forwards the event to the
clients connected to it — no instance needs to know where the other clients are attached. Because
the consumer group id is regenerated on every start, no offsets are carried over between restarts:
the notifications are volatile by design and only relevant to clients connected at that moment.
As with any jEAP messaging participant, the service registers producer and consumer
message contracts for the topic with the jEAP Message Contract Service.

```mermaid
flowchart LR
   UI2["UI 2"]
   UI1["UI 1"]

    subgraph MSX["Microservice X"]
        direction TB

        subgraph I2["Instance 2"]
            direction LR
            MS2["Resource API<br/>+ SSE library"]
            CG2(("consumer group 2"))
            MS2 --- CG2
        end

        subgraph I1["Instance 1"]
            direction LR
            MS1["Resource API<br/>+ SSE library"]
            CG1(("consumer group 1"))
            MS1 --- CG1
        end
    end

    CMD{{"NotifyClient Command"}}
    MCS["jEAP Message Contract Service"]

    UI2 -->|"1b GET SSE events"| MS2
    UI1 -->|"1a GET SSE events"| MS1

    UI2 -->|"2 delete resource"| MS2
    MS2 -->|"3 Kafka event<br/>RESOURCE_DELETED"| CMD

    CMD --> CG2
    CMD --> CG1

    MS2 -->|"4b SSE event<br/>RESOURCE_DELETED"| UI2
    MS1 -->|"4a SSE event<br/>RESOURCE_DELETED"| UI1

    MSX -->|"registerContracts"| MCS

    classDef green fill:#b6d7a8,stroke:#333333,stroke-width:1px
    classDef blue fill:#9fc5e8,stroke:#333333,stroke-width:1px
    classDef white fill:#ffffff,stroke:#333333,stroke-width:1px

    class UI1,UI2,MS1,MS2,MCS green
    class CMD blue
    class CG1,CG2 white

    style MSX fill:#eeeeee,stroke:#999999,stroke-dasharray:5 5
```

## The SSE endpoint

`NotifyClientController` exposes `GET ${jeap.sse.web.endpoint}` producing `text/event-stream`. Each
subscribing client gets its own `SseEmitter` (timeout `jeap.sse.web.emitter.timeoutInMs`) added to a
`CopyOnWriteArrayList`; emitters are removed on completion, timeout or error. A separate
`NotifyClientHeartbeatSender` pushes a `HEARTBEAT` event at `jeap.sse.web.heartbeat.rateInMs` to keep
intermediaries from closing idle connections.

## Why only references travel

By design an SSE event carries only an event type and a `resourcePath` reference, never the full
resource data. SSE streams cannot be covered by consumer-driven contract tests, so the client uses
the reference to fetch the current data with a normal, testable REST call.

## Related

- [Getting started](getting-started.md)
- [Configuration reference](configuration.md)
- [Client integration](client-integration.md)
- [jeap-server-sent-events](../README.md)
