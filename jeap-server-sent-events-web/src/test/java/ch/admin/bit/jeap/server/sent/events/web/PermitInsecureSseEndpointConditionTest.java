package ch.admin.bit.jeap.server.sent.events.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermitInsecureSseEndpointConditionTest {

    private AtomicReference<Boolean> envValue = new AtomicReference();
    private Environment environment;
    private ConditionContext conditionContext;

    private PermitInsecureSseEndpointCondition condition;

    @BeforeEach
    void setUp() {
        envValue.set(null);
        environment = new Environment() {
            @Override
            public String[] getActiveProfiles() {
                return new String[0];
            }

            @Override
            public String[] getDefaultProfiles() {
                return new String[0];
            }

            @Override
            public boolean acceptsProfiles(String... profiles) {
                return false;
            }

            @Override
            public boolean acceptsProfiles(Profiles profiles) {
                return false;
            }

            @Override
            public boolean containsProperty(String key) {
                return false;
            }

            @Override
            public String getProperty(String key) {
                return "";
            }

            @Override
            public String getProperty(String key, String defaultValue) {
                return "";
            }

            @Override
            public <T> T getProperty(String key, Class<T> targetType) {
                return null;
            }

            @Override
            public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
                if(!"jeap.sse.web.insecure.enabled".equals(key)) {
                    throw new IllegalArgumentException("Only jeap.sse.web.insecure.enabled allowed");
                }
                if(envValue.get() == null) {
                    return defaultValue;
                }
                return (T) envValue.get();
            }

            @Override
            public String getRequiredProperty(String key) throws IllegalStateException {
                return "";
            }

            @Override
            public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
                return null;
            }

            @Override
            public String resolvePlaceholders(String text) {
                return "";
            }

            @Override
            public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
                return "";
            }
        };
        conditionContext = mock(ConditionContext.class);
        when(conditionContext.getEnvironment()).thenReturn(environment);

        condition = new PermitInsecureSseEndpointCondition();
    }

    @Test
    void matchesPropertySet() {
        envValue.set(true);
        assertTrue(condition.matches(conditionContext, null));
    }

    @Test
    void matchesNotPropertySetToFalse() {
        envValue.set(false);
        assertFalse(condition.matches(conditionContext, null));
    }

    @Test
    void matchesNotPropertyNotSet() {
        assertFalse(condition.matches(conditionContext, null));
    }
}
