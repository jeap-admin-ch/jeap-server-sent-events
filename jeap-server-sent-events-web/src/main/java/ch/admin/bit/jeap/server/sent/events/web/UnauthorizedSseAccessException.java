package ch.admin.bit.jeap.server.sent.events.web;

public class UnauthorizedSseAccessException extends RuntimeException {

    private UnauthorizedSseAccessException(String msg) {
        super(msg);
    }

    public static UnauthorizedSseAccessException unauthorizedSseAccessException() {
        return unauthorizedSseAccessException("Unauthorized access to SSE endpoint");
    }

    public static UnauthorizedSseAccessException unauthorizedSseAccessException(String message) {
        return new UnauthorizedSseAccessException(message);
    }
}
