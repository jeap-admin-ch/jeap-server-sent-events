package ch.admin.bit.jeap.server.sent.events.spring;

import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventHandler;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationEventListener;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationListener;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@AutoConfiguration
@PropertySource("classpath:jeap-sse-defaults.properties")
@ConditionalOnProperty(name = "jeap.sse.enabled", havingValue = "true", matchIfMissing = true)
public class ServerSentEventsAutoConfiguration {

    @Bean
    public ResourceMutationService resourceMutationService(@Value("${spring.application.name}") String applicationName, List<ResourceMutationListener> resourceMutationListeners) {
        return new ResourceMutationService(applicationName, resourceMutationListeners);
    }

    @Bean
    public ResourceMutationEventHandler resourceMutationEventHandler(@Value("${spring.application.name}") String applicationName, List<ResourceMutationEventListener> resourceMutationEventListeners) {
        return new ResourceMutationEventHandler(applicationName, resourceMutationEventListeners);
    }
}
