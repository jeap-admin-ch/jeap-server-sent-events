package ch.admin.bit.jeap.server.sent.events.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestResponseExceptionHandlerTest {

    @Test
    void testHandleException() {
        RestResponseExceptionHandler handler = new RestResponseExceptionHandler();
        UnauthorizedSseAccessException exception = UnauthorizedSseAccessException.unauthorizedSseAccessException();

        ResponseEntity<String> responseEntity = handler.handleInvalidXMLInputException(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}
