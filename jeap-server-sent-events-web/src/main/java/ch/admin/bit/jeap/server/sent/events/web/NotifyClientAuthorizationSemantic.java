package ch.admin.bit.jeap.server.sent.events.web;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class NotifyClientAuthorizationSemantic implements NotifyClienAuthorization {

    private final String resource;
    private final String operation;
    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    @Override
    public void check() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JeapAuthenticationToken)) {
            throw UnauthorizedSseAccessException.unauthorizedSseAccessException("Not a JeapAuthenticationToken.");
        }

        if (!jeapSemanticAuthorization.hasRole(resource, operation)) {
            throw UnauthorizedSseAccessException.unauthorizedSseAccessException();
        }
    }
}
