package ch.admin.bit.jeap.server.sent.events.web;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PermitInsecureSseEndpointCondition implements Condition {

    private static final String SSE_PERMIT_INSECURE_PROPERTY = "jeap.sse.web.insecure.enabled";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Environment env = conditionContext.getEnvironment();
        return env.getProperty(SSE_PERMIT_INSECURE_PROPERTY, Boolean.class, false);
    }
}
