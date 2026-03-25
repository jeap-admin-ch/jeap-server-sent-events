package ch.admin.bit.jeap.server.sent.events.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    void sendEvent_noEmitters_doesNothing() {
        assertDoesNotThrow(() -> controller.sendEvent("test", "data"));
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
        assertEquals(3, capturedData.size());
    }

    @Test
    void sendEvent_whenIOException_removesEmitter() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);

        controller = new NotifyClientController(TIMEOUT, Optional.empty()) {
            @Override
            SseEmitter createEmitter(long emitterTimeout) {
                return emitter;
            }
        };

        controller.streamEvents();

        // simulate failure
        doThrow(new IOException("boom")).when(emitter).send(any(Set.class));

        controller.sendEvent("test", "data");

        verify(emitter).complete(); // important!
    }

    @Test
    void streamEvents_onCompletion_removesEmitter() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);

        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);

        controller = new NotifyClientController(TIMEOUT, Optional.empty()) {
            @Override
            SseEmitter createEmitter(long emitterTimeout) {
                return emitter;
            }
        };

        controller.streamEvents();

        verify(emitter).onCompletion(completionCaptor.capture());

        // simulate completion
        completionCaptor.getValue().run();

        // now send event → should NOT call emitter.send()
        controller.sendEvent("test", "data");

        verify(emitter, never()).send(any(Object.class));
    }

    @Test
    void streamEvents_onTimeout_removesEmitter() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        controller = new NotifyClientController(TIMEOUT, Optional.empty()) {
            @Override
            SseEmitter createEmitter(long emitterTimeout) {
                return emitter;
            }
        };

        controller.streamEvents();

        verify(emitter).onTimeout(captor.capture());
        captor.getValue().run();

        controller.sendEvent("test", "data");

        verify(emitter, never()).send(any(Object.class));
    }
}
