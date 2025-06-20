package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Slf4j
public class NotifyClientAuthorizationConfigurationValidator {

    private final boolean insecureEnabled;
    private final Optional<ServletSemanticAuthorization> jeapSemanticAuthorization;
    private final String resource;
    private final String operation;
    private final Optional<ServletSimpleAuthorization> simpleAuthorization;
    private final String role;

    @PostConstruct
    public void validate() {
        if (insecureEnabled && (hasText(resource) || hasText(operation) || hasText(role))) {
            log.warn("If insecure permit is, resource, operation or role should not be specified.");
            throw new IllegalArgumentException("If insecure permit is, resource, operation or role should not be specified.");
        }
        if (!insecureEnabled && !hasText(resource) && !hasText(operation) && !hasText(role)) {
            log.warn("Either set insecure permit, or specify resource and operation or role for authorization.");
            throw new IllegalArgumentException("Either set insecure permit, or specify resource and operation or role for authorization.");
        }
        if (hasText(resource) && hasText(operation) && hasText(role)) {
            log.warn("Resource and operation are specified but role is also specified. This is not allowed for semantic or simple authorization, please specify both or neither.");
            throw new IllegalArgumentException("Resource and operation are specified but role is also specified. This is not allowed for semantic or simple authorization, please specify both or neither.");
        }
        if (hasText(resource) && !hasText(operation)) {
            log.warn("Resource is specified but operation is not. This is not allowed for semantic authorization, please specify both or neither.");
            throw new IllegalArgumentException("Resource is specified but operation is not.");
        }
        if (hasText(operation) && !hasText(resource)) {
            log.warn("Operation is specified but resource is not. This is not allowed for semantic authorization, please specify both or neither.");
            throw new IllegalArgumentException("Resource is specified but operation is not.");
        }
        if (hasText(resource) && hasText(operation) && !jeapSemanticAuthorization.isPresent()) {
            log.warn("Resource and operation are specified but jeapSemanticAuthorization is not.");
            throw new IllegalArgumentException("Resource and operation are specified but jeapSemanticAuthorization is not.");
        }
        if (hasText(role) && !simpleAuthorization.isPresent()) {
            log.warn("Role is specified but simpleAuthorization is not.");
            throw new IllegalArgumentException("Role is specified but simpleAuthorization is not.");
        }
    }
}
