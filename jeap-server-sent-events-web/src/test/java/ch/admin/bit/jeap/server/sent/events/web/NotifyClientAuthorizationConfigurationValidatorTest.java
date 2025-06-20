package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SuppressWarnings("java:S5976")
class NotifyClientAuthorizationConfigurationValidatorTest {

    @Test
    void insecureSet() {
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.empty();
        String resource = "";
        String operation = "";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                true,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertDoesNotThrow(validator::validate);
    }

    @Test
    void nothingSet() {
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.empty();
        String resource = "";
        String operation = "";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }

    @Test
    void semanticAuthorizationSet() {
        ServletSemanticAuthorization jeaServletSemanticAuthorization = mock(ServletSemanticAuthorization.class);
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.of(jeaServletSemanticAuthorization);
        String resource = "sse";
        String operation = "read";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertDoesNotThrow(validator::validate);
    }

    @Test
    void simpleAuthorizationSet() {
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.empty();
        String resource = "";
        String operation = "";
        ServletSimpleAuthorization simpleAuthorizationInstance = mock(ServletSimpleAuthorization.class);
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.of(simpleAuthorizationInstance);
        String role = "read";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertDoesNotThrow(validator::validate);
    }

    @Test
    void allSet() {
        ServletSemanticAuthorization jeaServletSemanticAuthorization = mock(ServletSemanticAuthorization.class);
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.of(jeaServletSemanticAuthorization);
        String resource = "sse";
        String operation = "read";
        ServletSimpleAuthorization simpleAuthorizationInstance = mock(ServletSimpleAuthorization.class);
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.of(simpleAuthorizationInstance);
        String role = "read";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }

    @Test
    void resourceSetButOperationNot() {
        ServletSemanticAuthorization jeaServletSemanticAuthorization = mock(ServletSemanticAuthorization.class);
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.of(jeaServletSemanticAuthorization);
        String resource = "sse";
        String operation = "";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }

    @Test
    void operationSetButResourceNot() {
        ServletSemanticAuthorization jeaServletSemanticAuthorization = mock(ServletSemanticAuthorization.class);
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.of(jeaServletSemanticAuthorization);
        String resource = "";
        String operation = "read";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }

    @Test
    void operationAndResourceSetButAuthorizationNot() {
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.empty();
        String resource = "sse";
        String operation = "read";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }

    @Test
    void roleSetAuthorizationNot() {
        Optional<ServletSemanticAuthorization> jeapSemanticAuthorization = Optional.empty();
        String resource = "";
        String operation = "";
        Optional<ServletSimpleAuthorization> simpleAuthorization = Optional.empty();
        String role = "read";

        NotifyClientAuthorizationConfigurationValidator validator = new NotifyClientAuthorizationConfigurationValidator(
                false,
                jeapSemanticAuthorization,
                resource,
                operation,
                simpleAuthorization,
                role
        );
        assertThrows(IllegalArgumentException.class, validator::validate);
    }
}
