package ch.admin.bit.jeap.server.sent.events;

import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = TestApp.class, properties = "jeap.sse.enabled=false")
class ServerSentEventsDisabledIT extends KafkaIntegrationTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertFalse(applicationContext.containsBean("notifyClientCommandConsumer"));
        assertFalse(applicationContext.containsBean("notifyClientCommandProducer"));
        assertFalse(applicationContext.containsBean("notifyClientContractsValidator"));
        assertFalse(applicationContext.containsBean("notifyClientTopicValidator"));
        assertFalse(applicationContext.containsBean("resourceMutationService"));
        assertFalse(applicationContext.containsBean("resourceMutationEventHandler"));
        assertFalse(applicationContext.containsBean("notifyClientResourceMutationDataSender"));
        assertFalse(applicationContext.containsBean("notifyClientHeartbeatSender"));
    }

}
