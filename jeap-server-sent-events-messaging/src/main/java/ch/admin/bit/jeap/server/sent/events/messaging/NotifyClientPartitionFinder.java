package ch.admin.bit.jeap.server.sent.events.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class NotifyClientPartitionFinder implements AutoCloseable {

    private final Admin admin;

    public NotifyClientPartitionFinder(KafkaAdmin kafkaAdmin) {
        this(Admin.create(kafkaAdmin.getConfigurationProperties()));
    }

    NotifyClientPartitionFinder(Admin admin) {
        this.admin = admin;
    }

    /**
     * Resolves the current partitions when the listener is initialized. Partitions added later are picked up after a
     * listener or application restart.
     */
    public String[] partitions(String topic) {
        try {
            Map<String, TopicDescription> descriptions =
                    admin.describeTopics(List.of(topic)).allTopicNames().get();
            TopicDescription description = descriptions.get(topic);
            String[] partitions = description.partitions().stream()
                    .map(info -> Integer.toString(info.partition()))
                    .toArray(String[]::new);

            log.info("Assigning SSE consumer to partitions {} of topic {}", Arrays.toString(partitions), topic);
            return partitions;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw NotifyClientKafkaException.resolvingPartitionsFailed(topic, e);
        } catch (ExecutionException | KafkaException e) {
            throw NotifyClientKafkaException.resolvingPartitionsFailed(topic, e);
        }
    }

    @Override
    public void close() {
        admin.close();
    }
}
