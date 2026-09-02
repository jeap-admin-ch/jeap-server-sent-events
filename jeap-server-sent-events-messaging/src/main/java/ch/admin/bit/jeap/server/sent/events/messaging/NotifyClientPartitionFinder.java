package ch.admin.bit.jeap.server.sent.events.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class NotifyClientPartitionFinder {

    private final KafkaAdmin kafkaAdmin;

    /**
     * Resolves the current partitions when the listener is initialized. Partitions added later are picked up after a
     * listener or application restart.
     */
    public String[] partitions(String topic) {
        try {
            TopicDescription description = kafkaAdmin.describeTopics(topic).get(topic);
            String[] partitions = description.partitions().stream()
                    .map(info -> Integer.toString(info.partition()))
                    .toArray(String[]::new);

            log.info("Assigning SSE consumer to partitions {} of topic {}", Arrays.toString(partitions), topic);
            return partitions;
        } catch (KafkaException e) {
            throw NotifyClientKafkaException.resolvingPartitionsFailed(topic, e);
        }
    }
}
