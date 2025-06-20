package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClientAuthorizationSimpleTest {

    private static final String ROLE = "operation";

    private ServletSimpleAuthorization simpleAuthorization;

    private NotifyClientAuthorizationSimple notifyClientAuthorizationSimple;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(JeapAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        simpleAuthorization = mock(ServletSimpleAuthorization.class);
        notifyClientAuthorizationSimple = new NotifyClientAuthorizationSimple(ROLE, simpleAuthorization);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCheckWithValidAuthentication() {
        when(simpleAuthorization.hasRole(ROLE)).thenReturn(true);

        assertDoesNotThrow(() -> notifyClientAuthorizationSimple.check());
    }

    @Test
    void testCheckWithInvalidAuthentication() {
        when(simpleAuthorization.hasRole(ROLE)).thenReturn(false);

        assertThrows(UnauthorizedSseAccessException.class, () -> notifyClientAuthorizationSimple.check());
    }

    @Test
    void testCheckWithInvalidAuthenticationInSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(IllegalStateException.class, () -> notifyClientAuthorizationSimple.check());
    }

}
