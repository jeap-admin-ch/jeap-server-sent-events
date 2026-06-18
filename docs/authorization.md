# Authorization

The SSE endpoint can be secured in one of three mutually exclusive ways. The chosen mode is selected
purely by configuration under `jeap.sse.web.*`; the bean wiring in `ServerSentEventsWebAutoConfiguration`
picks the matching `NotifyClienAuthorization` implementation, and
`NotifyClientAuthorizationConfigurationValidator` rejects ambiguous or empty combinations at startup.

The native browser `EventSource` cannot send custom headers, so authentication for SSE typically
relies on a JWT carried as a cookie scoped to the endpoint path. See
[Client integration](client-integration.md) for the client-side implications.

## Insecure (development only)

```yaml
jeap:
  sse:
    web:
      insecure:
        enabled: true
```

Permits unauthenticated access. `SseApiWebSecurityConfig` registers a high-precedence
`SecurityFilterChain` that matches the endpoint and `permitAll()`. Use only locally or in tests; it is
mutually exclusive with the auth properties below.

## Simple (role-based)

```yaml
jeap:
  sse:
    web:
      auth:
        role: declaration-reader
```

`NotifyClientAuthorizationSimple` requires a `JeapAuthenticationToken` in the security context and
checks `ServletSimpleAuthorization.hasRole(role)`. Requires the jEAP security starter to provide a
`ServletSimpleAuthorization` bean; otherwise startup validation fails.

## Semantic (resource / operation)

```yaml
jeap:
  sse:
    web:
      auth:
        resource: declaration
        operation: read
```

`NotifyClientAuthorizationSemantic` requires a `JeapAuthenticationToken` and checks
`ServletSemanticAuthorization.hasRole(resource, operation)`. Both `resource` and `operation` must be
set together. Requires a `ServletSemanticAuthorization` bean from the jEAP security starter.

## Failure behaviour

When an authorization check fails, the controller throws `UnauthorizedSseAccessException`, mapped to
an HTTP error by `RestResponseExceptionHandler`. A missing or wrong token type (not a
`JeapAuthenticationToken`) is rejected before the role/resource check.

## Related

- [Configuration reference](configuration.md)
- [Client integration](client-integration.md)
- [Getting started](getting-started.md)
- [jeap-server-sent-events](../README.md)
