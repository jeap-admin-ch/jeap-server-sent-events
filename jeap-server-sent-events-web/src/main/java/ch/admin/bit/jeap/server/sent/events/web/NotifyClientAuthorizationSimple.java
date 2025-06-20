package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class NotifyClientAuthorizationSimple implements NotifyClienAuthorization {

    private final String role;
    private final ServletSimpleAuthorization simpleAuthorization;

    @Override
    public void check() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JeapAuthenticationToken)) {
            throw new IllegalStateException("Not a JeapAuthenticationToken.");
        }

        if (!simpleAuthorization.hasRole(role)) {
            throw UnauthorizedSseAccessException.unauthorizedSseAccessException();
        }

    }

}
