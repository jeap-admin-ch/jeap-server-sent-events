package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jeap.messaging.kafka.properties.KafkaProperties;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationListener;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class NotifyClientCommandProducer implements ResourceMutationListener {

    private final String topic;
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(kafkaProperties.getSystemName())) {
            throw new IllegalArgumentException("System name is required");
        }
    }

    @Override
    public void onResourceMutation(String applicationName, ResourceMutationType type, String resourcePath, String processId) {
        NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(
                kafkaProperties.getSystemName(),
                applicationName,
                resourcePath,
                convertToCommandType(type),
                processId
        );
        sendSync(topic, null, command);
    }

    private NotifyClientCommandType convertToCommandType(ResourceMutationType type) {
        return switch (type) {
            case RESOURCE_CREATED -> NotifyClientCommandType.RESOURCE_CREATED;
            case RESOURCE_UPDATED -> NotifyClientCommandType.RESOURCE_UPDATED;
            case RESOURCE_DELETED -> NotifyClientCommandType.RESOURCE_DELETED;
        };
    }

    private void sendSync(String topic, AvroMessageKey key, AvroMessage message) {
        try {
            kafkaTemplate.send(topic, key, message).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw NotifyClientKafkaException.producingCommandFailed(e);
        } catch (ExecutionException e) {
            throw NotifyClientKafkaException.producingCommandFailed(e);
        }
    }


}
