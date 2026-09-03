package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.TopicPartitionOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyClientPartitionMonitorTest {

    private final NotifyClientPartitionFinder partitionFinder = mock(NotifyClientPartitionFinder.class);
    private final NotifyClientCommandConsumer commandConsumer = mock(NotifyClientCommandConsumer.class);
    private final KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
    @SuppressWarnings("unchecked")
    private final ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory =
            mock(ConcurrentKafkaListenerContainerFactory.class);
    @SuppressWarnings("unchecked")
    private final ConcurrentMessageListenerContainer<Object, Object> primaryContainer =
            mock(ConcurrentMessageListenerContainer.class);
    @SuppressWarnings("unchecked")
    private final ConcurrentMessageListenerContainer<Object, Object> additionalContainer =
            mock(ConcurrentMessageListenerContainer.class);
    @SuppressWarnings("unchecked")
    private final RecordFilterStrategy<Object, Object> recordFilterStrategy = mock(RecordFilterStrategy.class);
    private final ContainerProperties additionalContainerProperties =
            new ContainerProperties(new TopicPartitionOffset("notify-client", 1));

    @Test
    void startsAConsumerForAnExistingPartitionAtTheEnd() {
        NotifyClientPartitionMonitor monitor = createMonitor();
        givenPrimaryPartition(0);
        when(partitionFinder.partitions("notify-client")).thenReturn(new String[]{"0", "1"});
        when(containerFactory.createContainer(any(TopicPartitionOffset[].class))).thenReturn(additionalContainer);
        when(additionalContainer.getContainerProperties()).thenReturn(additionalContainerProperties);

        monitor.refreshPartitions();

        ArgumentCaptor<TopicPartitionOffset[]> assignments = ArgumentCaptor.forClass(TopicPartitionOffset[].class);
        verify(containerFactory).createContainer(assignments.capture());
        assertThat(assignments.getValue()).singleElement().satisfies(assignment -> {
            assertThat(assignment.getTopic()).isEqualTo("notify-client");
            assertThat(assignment.getPartition()).isEqualTo(1);
            assertThat(assignment.getPosition()).isEqualTo(TopicPartitionOffset.SeekPosition.END);
        });
    }

    @Test
    void startsAConsumerForANewPartitionAtTheBeginning() {
        NotifyClientPartitionMonitor monitor = createMonitor();
        givenPrimaryPartition(0);
        when(partitionFinder.partitions("notify-client"))
                .thenReturn(new String[]{"0"}, new String[]{"0", "1"});
        when(containerFactory.createContainer(any(TopicPartitionOffset[].class))).thenReturn(additionalContainer);
        when(additionalContainer.getContainerProperties()).thenReturn(additionalContainerProperties);

        monitor.refreshPartitions();
        monitor.refreshPartitions();

        ArgumentCaptor<TopicPartitionOffset[]> assignments = ArgumentCaptor.forClass(TopicPartitionOffset[].class);
        verify(containerFactory).createContainer(assignments.capture());
        assertThat(assignments.getValue()).singleElement().satisfies(assignment -> {
            assertThat(assignment.getTopic()).isEqualTo("notify-client");
            assertThat(assignment.getPartition()).isEqualTo(1);
            assertThat(assignment.getPosition()).isEqualTo(TopicPartitionOffset.SeekPosition.BEGINNING);
        });
        verify(additionalContainer).setBeanName("testapp-sse-notify-client-partition-1");
        assertThat(additionalContainerProperties.getGroupId()).isNull();
        verify(additionalContainer).start();
        when(additionalContainer.getGroupId()).thenReturn(null);
        assertThat(monitor.consumesPartitionWithoutGroup(1)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void delegatesRecordsFromANewPartitionToTheCommandConsumer() {
        NotifyClientPartitionMonitor monitor = createMonitor();
        givenPrimaryPartition(0);
        when(partitionFinder.partitions("notify-client"))
                .thenReturn(new String[]{"0"}, new String[]{"0", "1"});
        when(containerFactory.createContainer(any(TopicPartitionOffset[].class))).thenReturn(additionalContainer);
        when(additionalContainer.getContainerProperties()).thenReturn(additionalContainerProperties);

        monitor.refreshPartitions();
        monitor.refreshPartitions();

        ArgumentCaptor<Object> listenerCaptor = ArgumentCaptor.forClass(Object.class);
        verify(additionalContainer).setupMessageListener(listenerCaptor.capture());
        assertThat(listenerCaptor.getValue()).isInstanceOf(FilteringMessageListenerAdapter.class);
        MessageListener<Object, Object> listener = (MessageListener<Object, Object>) listenerCaptor.getValue();
        NotifyClientCommand command = mock(NotifyClientCommand.class);
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("notify-client", 1, 0, null, command);
        listener.onMessage(record);
        verify(recordFilterStrategy).filter(record);
        verify(commandConsumer).consume(command);
    }

    @Test
    void doesNotStartAnotherConsumerForAnExistingPartition() {
        NotifyClientPartitionMonitor monitor = createMonitor();
        givenPrimaryPartition(0);
        when(partitionFinder.partitions("notify-client")).thenReturn(new String[]{"0"});

        monitor.refreshPartitions();

        verify(containerFactory, never()).createContainer(any(TopicPartitionOffset[].class));
    }

    private NotifyClientPartitionMonitor createMonitor() {
        return new NotifyClientPartitionMonitor("testapp", "notify-client", 30000, partitionFinder,
                commandConsumer, registry, containerFactory, recordFilterStrategy);
    }

    private void givenPrimaryPartition(int partition) {
        ContainerProperties properties = new ContainerProperties(
                new TopicPartitionOffset("notify-client", partition, TopicPartitionOffset.SeekPosition.END));
        when(registry.getListenerContainer("testapp-sse-notify-client")).thenReturn(primaryContainer);
        when(primaryContainer.getContainerProperties()).thenReturn(properties);
    }
}
