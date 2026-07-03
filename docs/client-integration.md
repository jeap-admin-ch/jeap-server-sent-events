# Client integration

A browser client subscribes to the SSE endpoint with the native `EventSource` API. The server keeps
the HTTP connection open and streams events; the browser reconnects automatically if the connection
drops.

## Event types

The server emits these named events on the stream:

| Event name         | Payload              | Meaning                                                 |
|--------------------|----------------------|---------------------------------------------------------|
| `RESOURCE_CREATED` | `{"path": "<ref>"}`  | A resource was created                                  |
| `RESOURCE_UPDATED` | `{"path": "<ref>"}`  | A resource was updated                                  |
| `RESOURCE_DELETED` | `{"path": "<ref>"}`  | A resource was deleted                                  |
| `HEARTBEAT`        | `{"interval": <ms>}` | Keep-alive sent every `jeap.sse.web.heartbeat.rateInMs` |

Each event also carries a random `id`. The `path` is a reference, not the resource data: the client
should fetch the current state with a normal REST call. This keeps the SSE contract minimal and
testable.

## Subscribing

```javascript
const source = new EventSource('/ui-api/sse/events', { withCredentials: true });

source.addEventListener('RESOURCE_CREATED', e => reload(JSON.parse(e.data).path));
source.addEventListener('RESOURCE_UPDATED', e => reload(JSON.parse(e.data).path));
source.addEventListener('RESOURCE_DELETED', e => remove(JSON.parse(e.data).path));

source.addEventListener('HEARTBEAT', () => { /* connection alive */ });

source.onerror = err => {
    // EventSource retries automatically; close it on permanent errors if needed
};
```

## Authentication

The native `EventSource` cannot set custom request headers, so a bearer token cannot be passed the
usual way. With a secured endpoint (see [Authorization](authorization.md)) the common approach is to
send the JWT as a cookie scoped to the endpoint path (`withCredentials: true`), backed by a matching
Spring Security configuration on the server. Where the events carry only triggers and no sensitive
data, running the endpoint unauthenticated may be acceptable. Browsers without a built-in
`EventSource` (or where header auth is required) can use a polyfill such as
[Yaffle/EventSource](https://github.com/Yaffle/EventSource).

## Related

- [Getting started](getting-started.md)
- [Authorization](authorization.md)
- [Architecture](architecture.md)
- [jeap-server-sent-events](../README.md)
