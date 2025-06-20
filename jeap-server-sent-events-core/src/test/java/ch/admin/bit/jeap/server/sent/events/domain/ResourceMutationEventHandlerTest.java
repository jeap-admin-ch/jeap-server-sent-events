package ch.admin.bit.jeap.server.sent.events.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ResourceMutationEventHandlerTest {

    private static final String APPLICATION_NAME = "testApp";

    private ResourceMutationEventHandler resourceMutationEventHandler;
    private List<ResourceMutationEventListener> listeners;

    @BeforeEach
    void setUp() {
        listeners = new ArrayList<>();
        resourceMutationEventHandler = new ResourceMutationEventHandler(APPLICATION_NAME, listeners);
    }

    @Test
    void resourceMutation() {
        ResourceMutationEvent event = new ResourceMutationEvent(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource");

        ResourceMutationEventListener listener = mock(ResourceMutationEventListener.class);
        listeners.add(listener);

        resourceMutationEventHandler.resourceMutation(event);

        verify(listener).onResourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");
    }

    @Test
    void resourceMutationDoNotCallListener_whenOtherApplication() {
        ResourceMutationEvent event = new ResourceMutationEvent(APPLICATION_NAME + "other", ResourceMutationType.RESOURCE_CREATED, "/test/resource");

        ResourceMutationEventListener listener = mock(ResourceMutationEventListener.class);
        listeners.add(listener);

        resourceMutationEventHandler.resourceMutation(event);

        verifyNoInteractions(listener);
    }

    @Test
    void resourceMutationSeveralListeners() {
        ResourceMutationEvent event = new ResourceMutationEvent(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource");

        ResourceMutationEventListener listener1 = mock(ResourceMutationEventListener.class);
        ResourceMutationEventListener listener2 = mock(ResourceMutationEventListener.class);
        ResourceMutationEventListener listener3 = mock(ResourceMutationEventListener.class);
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);

        resourceMutationEventHandler.resourceMutation(event);

        verify(listener1).onResourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");
        verify(listener2).onResourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");
        verify(listener3).onResourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");
    }
}
