package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotifyClientCommandBuilderTest {


    @Test
    void buildNotifyClientCommand() {
        String systemName = "testSystem";
        String serviceName = "testService";
        String resourcePath = "/test/resource";
        NotifyClientCommandType type = NotifyClientCommandType.RESOURCE_CREATED;
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(systemName, serviceName, resourcePath, type, null);


        assertNull(command.getProcessId());
        assertNotNull(command.getIdentity().getIdempotenceId());
        assertEquals(systemName, command.getPublisher().getSystem());
        assertEquals(serviceName, command.getPublisher().getService());

        assertEquals("/test/resource", command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_CREATED, command.getPayload().getType());
    }

    @Test
    void buildNotifyClientCommandWithProcessId() {
        String systemName = "testSystem";
        String serviceName = "testService";
        String resourcePath = "/test/resource";
        String processId = "testProcessId";
        NotifyClientCommandType type = NotifyClientCommandType.RESOURCE_CREATED;
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(systemName, serviceName, resourcePath, type, processId);


        assertEquals(processId, command.getProcessId());
        assertNotNull(command.getIdentity().getIdempotenceId());
        assertEquals(systemName, command.getPublisher().getSystem());
        assertEquals(serviceName, command.getPublisher().getService());

        assertEquals("/test/resource", command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_CREATED, command.getPayload().getType());
    }

}
