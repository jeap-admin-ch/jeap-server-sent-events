package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventListener;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NotifyClientResourceMutationDataSender implements ResourceMutationEventListener {

    private final NotifyClientController notifyClientController;
    private final ObjectMapper objectMapper;

    @Override
    public void onResourceMutation(ResourceMutationType type, String resourcePath) {
        String name = switch (type) {
            case RESOURCE_CREATED -> "RESOURCE_CREATED";
            case RESOURCE_UPDATED -> "RESOURCE_UPDATED";
            case RESOURCE_DELETED -> "RESOURCE_DELETED";
        };
        ResourceMutation resourceMutation = new ResourceMutation(resourcePath);
        try {
            String resourceEventData = objectMapper.writeValueAsString(resourceMutation);
            notifyClientController.sendEvent(name, resourceEventData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

    }

    private record ResourceMutation(String path) {
    }
}
