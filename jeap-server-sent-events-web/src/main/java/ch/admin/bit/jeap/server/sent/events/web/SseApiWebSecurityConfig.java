package ch.admin.bit.jeap.server.sent.events.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "jeap.sse.enabled", havingValue = "true", matchIfMissing = true)
@Conditional(PermitInsecureSseEndpointCondition.class)
public class SseApiWebSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      @Value("${jeap.sse.web.endpoint}") String webEndpoint) throws Exception {
        http.securityMatcher(webEndpoint)
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .cors(Customizer.withDefaults());


        return http.build();
    }
}
