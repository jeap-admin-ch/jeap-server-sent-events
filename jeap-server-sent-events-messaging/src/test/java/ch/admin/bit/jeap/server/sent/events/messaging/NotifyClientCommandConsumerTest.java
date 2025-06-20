package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEvent;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyClientCommandConsumerTest {

    private ResourceMutationEventHandler resourceMutationEventHandler;

    private NotifyClientCommandConsumer notifyClientCommandConsumer;

    @BeforeEach
    void setUp() {
        resourceMutationEventHandler = mock(ResourceMutationEventHandler.class);
        notifyClientCommandConsumer = new NotifyClientCommandConsumer(resourceMutationEventHandler);
    }

    @Test
    void consumeResourceCreated() {
        String systemName = "testSystem";
        String serviceName = "testService";
        String resourcePath = "/test/resource";
        NotifyClientCommandType type = NotifyClientCommandType.RESOURCE_CREATED;
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(systemName, serviceName, resourcePath, type, null);

        notifyClientCommandConsumer.consume(command);

        ArgumentCaptor<ResourceMutationEvent> captor = ArgumentCaptor.forClass(ResourceMutationEvent.class);
        verify(resourceMutationEventHandler).resourceMutation(captor.capture());

        ResourceMutationEvent value = captor.getValue();
        assertEquals(serviceName, value.sendingApplication());
        assertEquals(resourcePath, value.resourcePath());
        assertEquals(ResourceMutationType.RESOURCE_CREATED, value.type());
    }

    @Test
    void consumeResourceUpdated() {
        String systemName = "testSystem";
        String serviceName = "testService";
        String resourcePath = "/test/resource";
        NotifyClientCommandType type = NotifyClientCommandType.RESOURCE_UPDATED;
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(systemName, serviceName, resourcePath, type, null);

        notifyClientCommandConsumer.consume(command);

        ArgumentCaptor<ResourceMutationEvent> captor = ArgumentCaptor.forClass(ResourceMutationEvent.class);
        verify(resourceMutationEventHandler).resourceMutation(captor.capture());

        ResourceMutationEvent value = captor.getValue();
        assertEquals(serviceName, value.sendingApplication());
        assertEquals(resourcePath, value.resourcePath());
        assertEquals(ResourceMutationType.RESOURCE_UPDATED, value.type());
    }

    @Test
    void consumeResourceDeleted() {
        String systemName = "testSystem";
        String serviceName = "testService";
        String resourcePath = "/test/resource";
        NotifyClientCommandType type = NotifyClientCommandType.RESOURCE_DELETED;
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(systemName, serviceName, resourcePath, type, null);

        notifyClientCommandConsumer.consume(command);

        ArgumentCaptor<ResourceMutationEvent> captor = ArgumentCaptor.forClass(ResourceMutationEvent.class);
        verify(resourceMutationEventHandler).resourceMutation(captor.capture());

        ResourceMutationEvent value = captor.getValue();
        assertEquals(serviceName, value.sendingApplication());
        assertEquals(resourcePath, value.resourcePath());
        assertEquals(ResourceMutationType.RESOURCE_DELETED, value.type());
    }
}
