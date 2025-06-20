package ch.admin.bit.jeap.server.sent.events.web.spring;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.server.sent.events.web.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@AutoConfiguration
@ConditionalOnProperty(name = "jeap.sse.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "ch.admin.bit.jeap.server.sent.events.web")
public class ServerSentEventsWebAutoConfiguration {

    @Bean
    public NotifyClientResourceMutationDataSender notifyClientResourceMutationDataSender(NotifyClientController notifyClientController, ObjectMapper objectMapper) {
        return new NotifyClientResourceMutationDataSender(notifyClientController, objectMapper);
    }

    @Bean
    public NotifyClientHeartbeatSender notifyClientHeartbeatSender(@Value("${jeap.sse.web.heartbeat.rateInMs}") long rateInMs, NotifyClientController notifyClientController) {
        return new NotifyClientHeartbeatSender(notifyClientController, rateInMs);
    }

    @Bean
    public NotifyClienAuthorization notifyClientAuthorization(@Value("${jeap.sse.web.auth.resource:}") String resource,
                                                              @Value("${jeap.sse.web.auth.operation:}") String operation,
                                                              Optional<ServletSemanticAuthorization> semanticAuthorization,
                                                              @Value("${jeap.sse.web.auth.role:}") String role,
                                                              Optional<ServletSimpleAuthorization> simpleAuthorization) {
        if (hasText(resource) || hasText(operation)) {
            return new NotifyClientAuthorizationSemantic(resource, operation, semanticAuthorization.orElse(null));
        }
        if (hasText(role)) {
            return new NotifyClientAuthorizationSimple(role, simpleAuthorization.orElse(null));
        }
        return null;
    }

    @Bean
    public NotifyClientAuthorizationConfigurationValidator notifyClientControllerAuthenticationConfigurationValidator(@Value("${jeap.sse.web.insecure.enabled}") boolean insecureEnabled,
                                                                                                                      Optional<ServletSemanticAuthorization> jeapSemanticAuthorization,
                                                                                                                      @Value("${jeap.sse.web.auth.resource:}") String resource,
                                                                                                                      @Value("${jeap.sse.web.auth.operation:}") String operation,
                                                                                                                      Optional<ServletSimpleAuthorization> simpleAuthorization,
                                                                                                                      @Value("${jeap.sse.web.auth.role:}") String role) {
        return new NotifyClientAuthorizationConfigurationValidator(insecureEnabled, jeapSemanticAuthorization, resource, operation, simpleAuthorization, role);
    }
}
