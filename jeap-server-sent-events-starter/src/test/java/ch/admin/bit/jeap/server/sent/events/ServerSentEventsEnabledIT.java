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

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "jeap.sse.web.insecure.enabled=true")
class ServerSentEventsEnabledIT extends KafkaIntegrationTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private ResourceMutationService resourceMutationService;

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
    void testOneSseEvent() throws Exception {
        // Prepare to receive events
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 1);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "testResource");

        testClient.waitFor(5);

        // Assert event types
        testClient.expectEventTypes("RESOURCE_CREATED");
        // Assert payloads
        testClient.expectPayloads(Map.of("path", "testResource"));
    }


    @Test
    void testMultipleSseEvents() throws Exception {
        // Prepare to receive events
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 3);

        // Trigger events
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "test1");
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_UPDATED, "test2");
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_DELETED, "test3");

        testClient.waitFor(5);

        // Assert event types
        testClient.expectEventTypes(
                "RESOURCE_CREATED",
                "RESOURCE_UPDATED",
                "RESOURCE_DELETED"
        );

        // Assert payloads
        testClient.expectPayloads(
                Map.of("path", "test1"),
                Map.of("path", "test2"),
                Map.of("path", "test3")
        );
    }
}
