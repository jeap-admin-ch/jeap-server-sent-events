package ch.admin.bit.jeap.server.sent.events.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyClientControllerTest {

    private static final long TIMEOUT = 33L;

    private SseEmitter sseEmitter;

    private NotifyClientController controller;

    @BeforeEach
    void setUp() {
        controller = new NotifyClientController(TIMEOUT, Optional.empty()) {
            @Override
            SseEmitter createEmitter(long emitterTimeout) {
                return mock(SseEmitter.class);
            }
        };
    }

    @Test
    void testStreamEvents() throws IOException {
        SseEmitter sseEmitter = controller.streamEvents();
        String name = "gugu";
        String data = "{ \"path\": \"/test/resource\" }";

        ArgumentCaptor<Set<ResponseBodyEmitter.DataWithMediaType>> captor = ArgumentCaptor.forClass(Set.class);

        controller.sendEvent(name, data);

        verify(sseEmitter).send(captor.capture());

        Set<ResponseBodyEmitter.DataWithMediaType> capturedData = captor.getValue();
//        assertTrue(capturedData.size() == 1, "Captured data size should be 1");
//        ResponseBodyEmitter.DataWithMediaType dataWithMediaType = capturedData.iterator().next();
//        System.out.println(dataWithMediaType);
    }

}
