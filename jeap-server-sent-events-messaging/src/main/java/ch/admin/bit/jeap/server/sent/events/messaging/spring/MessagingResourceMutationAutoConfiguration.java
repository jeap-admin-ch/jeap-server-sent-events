package ch.admin.bit.jeap.server.sent.events.messaging.spring;

import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jeap.messaging.kafka.contract.ContractsProvider;
import ch.admin.bit.jeap.messaging.kafka.filter.ErrorHandlingTargetFilter;
import ch.admin.bit.jeap.messaging.kafka.properties.KafkaProperties;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientCommandConsumer;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientCommandProducer;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientContractsValidator;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientPartitionFinder;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientTopicValidator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

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

    @Bean
    public NotifyClientPartitionFinder notifyClientPartitionFinder(KafkaAdmin kafkaAdmin) {
        return new NotifyClientPartitionFinder(kafkaAdmin);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ConcurrentKafkaListenerContainerFactory<Object, Object> notifyClientKafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ErrorHandlingTargetFilter errorHandlingTargetFilter,
            KafkaAdmin kafkaAdmin,
            ObjectProvider<ContainerCustomizer> containerCustomizer,
            BeanFactory beanFactory) {

        Map<String, Object> consumerProperties = new HashMap<>(consumerFactory.getConfigurationProperties());
        consumerProperties.remove(ConsumerConfig.GROUP_ID_CONFIG);
        consumerProperties.remove(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        DefaultKafkaConsumerFactory<Object, Object> grouplessConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties);
        consumerFactory.getListeners().forEach(grouplessConsumerFactory::addListener);
        consumerFactory.getPostProcessors().forEach(grouplessConsumerFactory::addPostProcessor);

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, grouplessConsumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setAssignmentCommitOption(ContainerProperties.AssignmentCommitOption.NEVER);
        factory.getContainerProperties().setKafkaAwareTransactionManager(null);
        ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory = beanFactory.getBean(
                "kafkaListenerContainerFactory", ConcurrentKafkaListenerContainerFactory.class);
        factory.getContainerProperties().setObservationEnabled(
                kafkaListenerContainerFactory.getContainerProperties().isObservationEnabled());
        factory.setRecordInterceptor(kafkaListenerContainerFactory.getRecordInterceptor());
        factory.setRecordFilterStrategy(errorHandlingTargetFilter);
        factory.setAckDiscarded(false);
        factory.setContainerCustomizer(container -> {
            containerCustomizer.ifAvailable(customizer -> customizer.configure(container));
            ((ConcurrentMessageListenerContainer<Object, Object>) container).setKafkaAdmin(kafkaAdmin);
        });
        return factory;
    }
}
