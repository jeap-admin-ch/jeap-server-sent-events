package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEvent;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
public class NotifyClientCommandConsumer {

    private final ResourceMutationEventHandler resourceMutationEventHandler;

    @KafkaListener(topics = "${jeap.sse.kafka.topic}", id = "${spring.application.name}-${random.uuid}")
    public void consume(NotifyClientCommand notifyClientCommand) {
        String sendingApplication = notifyClientCommand.getPublisher().getService();
        NotifyClientCommandType type = notifyClientCommand.getPayload().getType();
        String resourcePath = notifyClientCommand.getReferences().getResourceReference().getResourcePath();
        ResourceMutationType mutationType = convertToMutationType(type);
        log.trace("Received NotifyClientCommand from application: {}, type: {}, resourcePath: {}", sendingApplication, mutationType, resourcePath);

        resourceMutationEventHandler.resourceMutation(new ResourceMutationEvent(sendingApplication, mutationType, resourcePath));
    }

    private ResourceMutationType convertToMutationType(NotifyClientCommandType type) {
        return switch (type) {
            case RESOURCE_CREATED -> ResourceMutationType.RESOURCE_CREATED;
            case RESOURCE_UPDATED -> ResourceMutationType.RESOURCE_UPDATED;
            case RESOURCE_DELETED -> ResourceMutationType.RESOURCE_DELETED;
        };
    }
}
