package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.messaging.avro.AvroMessageType;
import ch.admin.bit.jeap.messaging.kafka.contract.ContractsValidator;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyClientContractsValidatorTest {

    @Test
    void checkContracts() {
        String topicName = "testTopic";
        ContractsValidator contractsValidator = mock(ContractsValidator.class);

        NotifyClientContractsValidator validator = new NotifyClientContractsValidator(contractsValidator, topicName);

        validator.checkContracts();

        verify(contractsValidator).ensurePublisherContract(any(AvroMessageType.class), eq(topicName));
        verify(contractsValidator).ensureConsumerContract(any(AvroMessageType.class), eq(topicName));
    }

}
