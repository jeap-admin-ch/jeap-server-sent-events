package ch.admin.bit.jeap.server.sent.events.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Service to handle resource mutations and notify listeners.
 * The calls are coming from business logic, sending to messaging only.
 */
@Slf4j
@RequiredArgsConstructor
public class ResourceMutationService {

    private final String applicationName;
    private final List<ResourceMutationListener> listeners;

    public void resourceMutation(ResourceMutationType type, String resourcePath) {
        resourceMutation(type, resourcePath, null);
    }

    public void resourceMutation(ResourceMutationType type, String resourcePath, String processId) {
        listeners.forEach(listener -> listener.onResourceMutation(applicationName, type, resourcePath, processId));
    }

}
