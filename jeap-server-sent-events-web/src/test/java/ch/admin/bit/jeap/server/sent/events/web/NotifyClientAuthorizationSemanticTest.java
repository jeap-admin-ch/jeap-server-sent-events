package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClientAuthorizationSemanticTest {

    private static final String RESOURCE = "resource";
    private static final String OPERATION = "operation";

    private ServletSemanticAuthorization jeapSemanticAuthorization;

    private NotifyClientAuthorizationSemantic notifyClientAuthorizationSemantic;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(JeapAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        jeapSemanticAuthorization = mock(ServletSemanticAuthorization.class);
        notifyClientAuthorizationSemantic = new NotifyClientAuthorizationSemantic(RESOURCE, OPERATION, jeapSemanticAuthorization);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCheckWithValidAuthentication() {
        when(jeapSemanticAuthorization.hasRole(RESOURCE, OPERATION)).thenReturn(true);

        assertDoesNotThrow(() -> notifyClientAuthorizationSemantic.check());
    }

    @Test
    void testCheckWithInvalidAuthentication() {
        when(jeapSemanticAuthorization.hasRole(RESOURCE, OPERATION)).thenReturn(false);

        assertThrows(UnauthorizedSseAccessException.class, () -> notifyClientAuthorizationSemantic.check());
    }

    @Test
    void testCheckWithInvalidAuthenticationInSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(UnauthorizedSseAccessException.class, () -> notifyClientAuthorizationSemantic.check());
    }

}
