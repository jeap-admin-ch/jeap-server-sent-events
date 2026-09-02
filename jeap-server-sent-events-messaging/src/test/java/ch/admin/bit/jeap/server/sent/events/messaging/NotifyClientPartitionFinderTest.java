package ch.admin.bit.jeap.server.sent.events.messaging;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClientPartitionFinderTest {

    @Test
    void resolvesAllTopicPartitions() {
        KafkaAdmin kafkaAdmin = mock(KafkaAdmin.class);
        TopicDescription topicDescription = mock(TopicDescription.class);
        TopicPartitionInfo partitionZero = mock(TopicPartitionInfo.class);
        TopicPartitionInfo partitionTwo = mock(TopicPartitionInfo.class);
        when(partitionZero.partition()).thenReturn(0);
        when(partitionTwo.partition()).thenReturn(2);
        when(topicDescription.partitions()).thenReturn(List.of(partitionZero, partitionTwo));
        when(kafkaAdmin.describeTopics("notify-client")).thenReturn(Map.of("notify-client", topicDescription));

        String[] partitions = new NotifyClientPartitionFinder(kafkaAdmin).partitions("notify-client");

        assertThat(partitions).containsExactly("0", "2");
    }

    @Test
    void wrapsPartitionResolutionFailure() {
        KafkaAdmin kafkaAdmin = mock(KafkaAdmin.class);
        KafkaException cause = new KafkaException("broker unavailable");
        when(kafkaAdmin.describeTopics("notify-client")).thenThrow(cause);

        assertThatThrownBy(() -> new NotifyClientPartitionFinder(kafkaAdmin).partitions("notify-client"))
                .isInstanceOf(NotifyClientKafkaException.class)
                .hasMessage("Failed to resolve partitions for topic notify-client")
                .hasCause(cause);
    }
}
