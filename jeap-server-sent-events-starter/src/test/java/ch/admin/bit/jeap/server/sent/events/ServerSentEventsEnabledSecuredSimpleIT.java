package ch.admin.bit.jeap.server.sent.events;

import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationContext;
import ch.admin.bit.jeap.security.test.jws.JwsBuilderFactory;
import ch.admin.bit.jeap.security.test.resource.configuration.JeapOAuth2IntegrationTestResourceConfiguration;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationService;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientTopicValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("secured-simple")
@Import(JeapOAuth2IntegrationTestResourceConfiguration.class)
class ServerSentEventsEnabledSecuredSimpleIT extends KafkaIntegrationTestBase {

    private static final String SIMPLE_AUTH_READ_ROLE = "authentication:read";

    private static final String SUBJECT = "69368608-D736-43C8-5F76-55B7BF168299";

    @LocalServerPort
    private int port;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private ResourceMutationService resourceMutationService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JwsBuilderFactory jwsBuilderFactory;

    @MockitoBean
    private NotifyClientTopicValidator notifyClientTopicValidator;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/" + applicationName;
    }

    @Test
    void authorized() throws Exception {
        String token = createJeapTokenWithUserRoles(SIMPLE_AUTH_READ_ROLE);
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 1, token);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "testResource");

        testClient.waitFor(5);

        // Assert event types
        testClient.expectEventTypes("RESOURCE_CREATED");
        // Assert payloads
        testClient.expectPayloads(Map.of("path", "testResource"));
    }

    @Test
    void nonAuthorizedOtherAuthenticationToken() throws Exception {
        String token = createJeapTokenWithUserRoles(SIMPLE_AUTH_READ_ROLE + "_other");
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 1, token);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "testResource");

        testClient.expectHttpStatusCode(1, 401);
    }

    @Test
    void nonAuthorizedNoAuthenticationToken() throws Exception {
        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, "/ui-api/sse/events", 1);

        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, "testResource");

        testClient.expectHttpStatusCode(1, 401);
    }

    private String createJeapTokenWithUserRoles(String... roles) {
        return jwsBuilderFactory.createValidForFixedLongPeriodBuilder(SUBJECT, JeapAuthenticationContext.SYS).
                withUserRoles(roles).
                build().serialize();
    }

}
