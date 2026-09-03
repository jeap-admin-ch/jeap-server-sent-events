package ch.admin.bit.jeap.server.sent.events.messaging;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClientPartitionFinderTest {

    @Test
    void resolvesAllTopicPartitions() {
        Admin admin = mock(Admin.class);
        DescribeTopicsResult describeTopicsResult = mock(DescribeTopicsResult.class);
        TopicDescription topicDescription = mock(TopicDescription.class);
        TopicPartitionInfo partitionZero = mock(TopicPartitionInfo.class);
        TopicPartitionInfo partitionTwo = mock(TopicPartitionInfo.class);
        when(partitionZero.partition()).thenReturn(0);
        when(partitionTwo.partition()).thenReturn(2);
        when(topicDescription.partitions()).thenReturn(List.of(partitionZero, partitionTwo));
        when(describeTopicsResult.allTopicNames())
                .thenReturn(KafkaFuture.completedFuture(Map.of("notify-client", topicDescription)));
        when(admin.describeTopics(List.of("notify-client"))).thenReturn(describeTopicsResult);

        String[] partitions = new NotifyClientPartitionFinder(admin).partitions("notify-client");

        assertThat(partitions).containsExactly("0", "2");
    }

    @Test
    void wrapsPartitionResolutionFailure() {
        Admin admin = mock(Admin.class);
        KafkaException cause = new KafkaException("broker unavailable");
        when(admin.describeTopics(List.of("notify-client"))).thenThrow(cause);

        assertThatThrownBy(() -> new NotifyClientPartitionFinder(admin).partitions("notify-client"))
                .isInstanceOf(NotifyClientKafkaException.class)
                .hasMessage("Failed to resolve partitions for topic notify-client")
                .hasCause(cause);
    }
}
