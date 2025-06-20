package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;

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

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertTrue(applicationContext.containsBean("notifyClientCommandConsumer"));
        assertTrue(applicationContext.containsBean("notifyClientCommandProducer"));
        assertTrue(applicationContext.containsBean("notifyClientContractsValidator"));
        assertTrue(applicationContext.containsBean("notifyClientTopicValidator"));
    }

}
