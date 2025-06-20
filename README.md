# jEAP server sent events

jEAP server sent events is a library that provides a way to send real-time events from the server to the client using
Server-Sent Events (SSE). It allows for efficient and scalable communication between the server and the client, enabling    
real-time updates and notifications.

## Properties

| Name                             | Remarks                                                                                                                | Default           | Mandatory         |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------|-------------------|-------------------|
| jeap.sse.kafka.topic             | Kafka topic to which the jEAP SSE Web Server will subscribe and publish, should be system-applicationname-notifyclient | -                 | yes               |
| jeap.sse.enabled                 | Flag to enable/disable the jEAP Server-Sent Events (SSE) support                                                       | true              | no                |
| jeap.sse.web.insecure.enabled    | Flag to enable/disable insecure access to sse endpoint                                                                 | false             | no                |
| jeap.sse.web.endpoint            | Endpoint for the jEAP SSE Web Server                                                                                   | ui-api/sse/events | no                |
| jeap.sse.web.auth.role           | Role required to access the jEAP SSE Web Server if set, for simple authentication                                      | -                 | no                |
| jeap.sse.web.auth.resource       | Resource required to access the jEAP SSE Web Server if set, for semantic authentication                                | -                 | no                |
| jeap.sse.web.auth.operation      | Operation required to access the jEAP SSE Web Server if set, for semantic authentication                               | -                 | no                |
| jeap.sse.web.heartbeat.rateInMs  | Rate in milliseconds for the heartbeat messages sent by the jEAP SSE Web Server                                        | 5000              | no                |
| jeap.sse.web.emitter.timeoutInMs | Timeout in milliseconds for the jEAP SSE Web Server emitter                                                            | 30000             | no                |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
