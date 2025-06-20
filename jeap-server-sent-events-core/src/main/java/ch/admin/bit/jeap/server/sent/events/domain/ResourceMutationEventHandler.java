package ch.admin.bit.jeap.server.sent.events.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * This handler is used to handle resource mutation events that are coming in via messaging and sends them out to web.
 * It filters out messages that are not relevant for the current application instance.
 */
@Slf4j
@RequiredArgsConstructor
public class ResourceMutationEventHandler {

    private final String applicationName;
    private final List<ResourceMutationEventListener> listeners;

    public void resourceMutation(ResourceMutationEvent resourceMutationEvent) {
        if (!Objects.equals(applicationName, resourceMutationEvent.sendingApplication())) {
            // We are not interested in messages from other applications
            return;
        }

        ResourceMutationType type = resourceMutationEvent.type();
        String resourcePath = resourceMutationEvent.resourcePath();
        log.trace("Notifying {} for resource at path: {}", type, resourcePath);
        listeners.forEach(resourceMutationListener -> resourceMutationListener.onResourceMutation(type, resourcePath));
    }
}
