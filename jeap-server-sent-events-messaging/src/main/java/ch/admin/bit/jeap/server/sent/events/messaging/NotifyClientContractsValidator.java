package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.messaging.avro.AvroMessageType;
import ch.admin.bit.jeap.messaging.kafka.contract.ContractsProvider;
import ch.admin.bit.jeap.messaging.kafka.contract.ContractsValidator;
import ch.admin.bit.jeap.messaging.kafka.contract.DefaultContractsValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotifyClientContractsValidator {

    private static final AvroMessageType NOTIFY_CLIENT_COMMAND = AvroMessageType.newBuilder()
            .setName(NotifyClientCommand.getClassSchema().getName())
            .setVersion(NotifyClientCommand.MESSAGE_TYPE_VERSION$)
            .build();

    private final ContractsValidator contractsValidator;
    private final String topicName;

    public NotifyClientContractsValidator(String applicationName, String topicName, ContractsProvider contractsProvider) {
        this(new DefaultContractsValidator(applicationName, contractsProvider), topicName);
    }


    @PostConstruct
    public void checkContracts() {
        contractsValidator.ensurePublisherContract(NOTIFY_CLIENT_COMMAND, topicName);
        contractsValidator.ensureConsumerContract(NOTIFY_CLIENT_COMMAND, topicName);
    }
}
