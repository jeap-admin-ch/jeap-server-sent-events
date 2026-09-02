package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestApp.class, properties = "jeap.sse.enabled=true")
@EmbeddedKafka(
        controlledShutdown = true,
        partitions = 1,
        topics = {"jeap-testapp-notifyclient"}
)
class ServerSentEventsMessagingIT extends KafkaIntegrationTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("notifyClientKafkaListenerContainerFactory")
    private ConcurrentKafkaListenerContainerFactory<Object, Object> notifyClientKafkaListenerContainerFactory;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertTrue(applicationContext.containsBean("notifyClientCommandConsumer"));
        assertTrue(applicationContext.containsBean("notifyClientCommandProducer"));
        assertTrue(applicationContext.containsBean("notifyClientContractsValidator"));
        assertTrue(applicationContext.containsBean("notifyClientTopicValidator"));
        assertTrue(applicationContext.containsBean("notifyClientPartitionFinder"));
        assertTrue(applicationContext.containsBean("notifyClientKafkaListenerContainerFactory"));
    }

    @Test
    void notifyClientListenerUsesExplicitAssignmentWithoutConsumerGroup() throws Exception {
        assertThat(notifyClientKafkaListenerContainerFactory.getConsumerFactory().getConfigurationProperties())
                .doesNotContainKey(ConsumerConfig.GROUP_ID_CONFIG)
                .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        assertThat(notifyClientKafkaListenerContainerFactory.getContainerProperties().getAckMode())
                .isEqualTo(ContainerProperties.AckMode.MANUAL);
        assertThat(notifyClientKafkaListenerContainerFactory.getContainerProperties().getAssignmentCommitOption())
                .isEqualTo(ContainerProperties.AssignmentCommitOption.NEVER);

        MessageListenerContainer listenerContainer =
                kafkaListenerEndpointRegistry.getListenerContainer("testapp-sse-notify-client");
        assertThat(listenerContainer).isInstanceOf(ConcurrentMessageListenerContainer.class);
        ConcurrentMessageListenerContainer<?, ?> concurrentContainer =
                (ConcurrentMessageListenerContainer<?, ?>) listenerContainer;
        assertThat(concurrentContainer.getConcurrency()).isEqualTo(1);
        assertThat(concurrentContainer.getGroupId()).isNull();
        assertThat(concurrentContainer.getAssignedPartitions())
                .containsExactly(new TopicPartition("jeap-testapp-notifyclient", 0));
        assertThat(concurrentContainer.getContainerProperties().getTopicPartitions())
                .allSatisfy(partition -> assertThat(partition.getPosition())
                        .isEqualTo(TopicPartitionOffset.SeekPosition.END));

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> groupIds = adminClient.listGroups().all().get().stream()
                    .map(group -> group.groupId())
                    .collect(Collectors.toSet());
            assertThat(groupIds).noneMatch(groupId -> groupId.startsWith("testapp-"));
        }
    }

}
