package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.GroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestApp.class, properties = {
        "jeap.sse.enabled=true",
        "jeap.sse.kafka.partitionRefreshRateInMs=100"
})
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

    @Autowired
    private NotifyClientPartitionMonitor notifyClientPartitionMonitor;

    @Autowired
    private KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate;

    @MockitoBean
    private ResourceMutationEventHandler resourceMutationEventHandler;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertTrue(applicationContext.containsBean("notifyClientCommandConsumer"));
        assertTrue(applicationContext.containsBean("notifyClientCommandProducer"));
        assertTrue(applicationContext.containsBean("notifyClientContractsValidator"));
        assertTrue(applicationContext.containsBean("notifyClientTopicValidator"));
        assertTrue(applicationContext.containsBean("notifyClientPartitionFinder"));
        assertTrue(applicationContext.containsBean("notifyClientPartitionMonitor"));
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
                    .map(GroupListing::groupId)
                    .collect(Collectors.toSet());
            assertThat(groupIds).noneMatch(groupId -> groupId.startsWith("testapp-"));
        }
    }

    @Test
    void consumesPartitionsAddedAtRuntimeWithoutAConsumerGroup() throws Exception {
        notifyClientPartitionMonitor.refreshPartitions();
        notifyClientPartitionMonitor.stop();
        try {
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                adminClient.createPartitions(Map.of("jeap-testapp-notifyclient", NewPartitions.increaseTo(2)))
                        .all().get();
            }

            NotifyClientCommand command = NotifyClientCommandBuilder.buildCommand(
                    "test-system", "testapp", "/new-partition", NotifyClientCommandType.RESOURCE_CREATED, null);
            kafkaTemplate.send("jeap-testapp-notifyclient", 1, null, command).get();
        } finally {
            notifyClientPartitionMonitor.start();
        }

        await().atMost(Duration.ofSeconds(10))
                .until(() -> notifyClientPartitionMonitor.consumesPartitionWithoutGroup(1));

        verify(resourceMutationEventHandler, timeout(10000)).resourceMutation(argThat(event ->
                event.resourcePath().equals("/new-partition")));

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> groupIds = adminClient.listGroups().all().get().stream()
                    .map(GroupListing::groupId)
                    .collect(Collectors.toSet());
            assertThat(groupIds).noneMatch(groupId -> groupId.startsWith("testapp-"));
        }
    }

}
