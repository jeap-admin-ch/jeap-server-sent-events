package ch.admin.bit.jeap.server.sent.events.messaging.spring;

import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jeap.messaging.kafka.contract.ContractsProvider;
import ch.admin.bit.jeap.messaging.kafka.properties.KafkaProperties;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientCommandConsumer;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientCommandProducer;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientContractsValidator;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientTopicValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnProperty(name = "jeap.sse.enabled", havingValue = "true", matchIfMissing = true)
public class MessagingResourceMutationAutoConfiguration {

    @Bean
    public NotifyClientCommandConsumer notifyClientCommandConsumer(@Lazy ResourceMutationEventHandler resourceMutationEventHandler) {
        return new NotifyClientCommandConsumer(resourceMutationEventHandler);
    }

    @Bean
    public NotifyClientCommandProducer notifyClientCommandProducer(@Value("${jeap.sse.kafka.topic}") String topic,
                                                                   KafkaProperties kafkaProperties,
                                                                   KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate) {
        return new NotifyClientCommandProducer(topic, kafkaProperties, kafkaTemplate);
    }

    @Bean
    public NotifyClientContractsValidator notifyClientContractsValidator(@Value("${spring.application.name}") String applicationName,
                                                                         @Value("${jeap.sse.kafka.topic}") String topicName,
                                                                         ContractsProvider contractsProvider) {
        return new NotifyClientContractsValidator(applicationName, topicName, contractsProvider);
    }

    @Bean
    public NotifyClientTopicValidator notifyClientTopicValidator(@Value("${jeap.sse.kafka.topic}") String topicName,
                                                                 KafkaAdmin kafkaAdmin) {
        return new NotifyClientTopicValidator(topicName, kafkaAdmin);
    }
}
