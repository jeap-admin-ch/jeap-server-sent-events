package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEvent;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;

@Slf4j
@RequiredArgsConstructor
public class NotifyClientCommandConsumer {

    private final ResourceMutationEventHandler resourceMutationEventHandler;

    // Partition 0 starts without querying Kafka; the monitor assigns all other partitions.
    @KafkaListener(
            id = "${spring.application.name}-sse-notify-client",
            idIsGroup = false,
            containerFactory = "notifyClientKafkaListenerContainerFactory",
            concurrency = "1",
            topicPartitions = @TopicPartition(
                    topic = "${jeap.sse.kafka.topic}",
                    partitionOffsets = @PartitionOffset(
                            partition = "0",
                            initialOffset = "0",
                            seekPosition = "END")))
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
