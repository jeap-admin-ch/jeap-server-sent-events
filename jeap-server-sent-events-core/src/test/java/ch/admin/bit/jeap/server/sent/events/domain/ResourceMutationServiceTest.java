package ch.admin.bit.jeap.server.sent.events.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ResourceMutationServiceTest {

    private static final String APPLICATION_NAME = "testApp";

    private ResourceMutationService resourceMutationService;
    private List<ResourceMutationListener> listeners;

    @BeforeEach
    void setUp() {
        listeners = new ArrayList<>();
        resourceMutationService = new ResourceMutationService(APPLICATION_NAME, listeners);
    }

    @Test
    void resourceMutation() {
        ResourceMutationListener listener = mock(ResourceMutationListener.class);
        listeners.add(listener);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");

        verify(listener).onResourceMutation(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource", null);
    }

    @Test
    void resourceMutationWithProcessId() {
        String processId = "process123";
        ResourceMutationListener listener = mock(ResourceMutationListener.class);
        listeners.add(listener);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource", processId);

        verify(listener).onResourceMutation(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource", processId);
    }


    @Test
    void resourceMutationSeveralListeners() {
        ResourceMutationListener listener1 = mock(ResourceMutationListener.class);
        ResourceMutationListener listener2 = mock(ResourceMutationListener.class);
        ResourceMutationListener listener3 = mock(ResourceMutationListener.class);
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "/test/resource");

        verify(listener1).onResourceMutation(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource", null);
        verify(listener2).onResourceMutation(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource", null);
        verify(listener3).onResourceMutation(APPLICATION_NAME, ResourceMutationType.RESOURCE_CREATED, "/test/resource", null);
    }
}
