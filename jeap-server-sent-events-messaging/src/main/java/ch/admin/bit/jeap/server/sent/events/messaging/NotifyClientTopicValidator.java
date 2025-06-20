package ch.admin.bit.jeap.server.sent.events.messaging;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j
public class NotifyClientTopicValidator {

    private final String topicName;
    private final KafkaAdmin kafkaAdmin;

    @PostConstruct
    public void checkIfTopicsExist() throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            List<String> topics = Collections.singletonList(topicName);
            log.info("Checking if topic exists: {}", topics);
            Map<String, TopicDescription> stringTopicDescriptionMap = adminClient.describeTopics(topics).allTopicNames().get();
            stringTopicDescriptionMap.forEach((name, desc) -> log.info("{}: {}", name, desc));
            log.info("Topic exists, good to go");
        }
    }
}
