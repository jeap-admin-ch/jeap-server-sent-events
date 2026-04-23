package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventListener;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
public class NotifyClientResourceMutationDataSender implements ResourceMutationEventListener {

    private final NotifyClientController notifyClientController;
    private final JsonMapper jsonMapper;

    @Override
    public void onResourceMutation(ResourceMutationType type, String resourcePath) {
        String name = switch (type) {
            case RESOURCE_CREATED -> "RESOURCE_CREATED";
            case RESOURCE_UPDATED -> "RESOURCE_UPDATED";
            case RESOURCE_DELETED -> "RESOURCE_DELETED";
        };
        ResourceMutation resourceMutation = new ResourceMutation(resourcePath);
        String resourceEventData = jsonMapper.writeValueAsString(resourceMutation);
        notifyClientController.sendEvent(name, resourceEventData);
    }

    private record ResourceMutation(String path) {
    }
}
