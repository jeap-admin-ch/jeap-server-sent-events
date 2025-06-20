package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyClientResourceMutationDataSenderTest {

    private NotifyClientController notifyClientController;

    private NotifyClientResourceMutationDataSender notifyClientResourceMutationDataSender;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        notifyClientController = mock(NotifyClientController.class);
        notifyClientResourceMutationDataSender = new NotifyClientResourceMutationDataSender(notifyClientController, objectMapper);
    }

    @Test
    void testResourceCreated() {
        String resourcePath = "/test/resource";

        notifyClientResourceMutationDataSender.onResourceMutation(ResourceMutationType.RESOURCE_CREATED, resourcePath);

        verify(notifyClientController).sendEvent("RESOURCE_CREATED", "{\"path\":\"/test/resource\"}");
    }

    @Test
    void testResourceUpdated() {
        String resourcePath = "/test/resource";

        notifyClientResourceMutationDataSender.onResourceMutation(ResourceMutationType.RESOURCE_UPDATED, resourcePath);

        verify(notifyClientController).sendEvent("RESOURCE_UPDATED", "{\"path\":\"/test/resource\"}");
    }

    @Test
    void testResourceDeleted() {
        String resourcePath = "/test/resource";

        notifyClientResourceMutationDataSender.onResourceMutation(ResourceMutationType.RESOURCE_DELETED, resourcePath);

        verify(notifyClientController).sendEvent("RESOURCE_DELETED", "{\"path\":\"/test/resource\"}");
    }
}
