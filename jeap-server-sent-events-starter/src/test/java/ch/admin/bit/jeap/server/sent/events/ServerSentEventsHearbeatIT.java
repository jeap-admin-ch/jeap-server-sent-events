package ch.admin.bit.jeap.server.sent.events;

import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationService;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientTopicValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jeap.sse.web.heartbeat.rateInMs=200",
                "jeap.sse.web.insecure.enabled=true"
        })
class ServerSentEventsHearbeatIT extends KafkaIntegrationTestBase {

    @LocalServerPort
    private int port;

    @MockitoBean
    private NotifyClientTopicValidator notifyClientTopicValidator;

    @Value("${spring.application.name}")
    private String applicationName;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/" + applicationName;
    }

    @Test
    void testOneHeartbeatEvent() throws Exception {
        // Prepare to receive events
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 1);

        testClient.waitFor(5);

        // Assert event types
        testClient.expectEventTypes("HEARTBEAT");
        // Assert payloads
        testClient.expectPayloads(Map.of("interval", "200"));
    }


}
