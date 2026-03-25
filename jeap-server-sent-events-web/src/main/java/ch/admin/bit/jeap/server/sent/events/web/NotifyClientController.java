package ch.admin.bit.jeap.server.sent.events.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@ConditionalOnProperty(name = "jeap.sse.enabled", havingValue = "true", matchIfMissing = true)
public class NotifyClientController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final long emitterTimeoutInMs;
    private final Optional<NotifyClienAuthorization> authorization;

    public NotifyClientController(@Value("${jeap.sse.web.emitter.timeoutInMs}") long emitterTimeoutInMs, Optional<NotifyClienAuthorization> authorization) {
        this.emitterTimeoutInMs = emitterTimeoutInMs;
        this.authorization = authorization;
    }

    @GetMapping(value = "${jeap.sse.web.endpoint}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        authorization.ifPresent(NotifyClienAuthorization::check);

        log.trace("Creating new SseEmitter with timeout {} ms", emitterTimeoutInMs);
        SseEmitter emitter = createEmitter(emitterTimeoutInMs);

        // Add the emitter to the list of active emitters
        emitters.add(emitter);
        log.trace("SseEmitter added, active emitter count: {}", emitters.size());

        // Remove the emitter when it completes (successfully or with error)
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.trace("SseEmitter completed and removed, active emitter count: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.trace("SseEmitter timed out and removed, active emitter count: {}", emitters.size());
        });
        emitter.onError(ex -> {
            emitters.remove(emitter);
            log.trace("SseEmitter error and removed ({}), active emitter count: {}", ex.getMessage(), emitters.size());
        });

        return emitter;
    }

    void sendEvent(String name, String data) {
        int emitterCount = emitters.size();
        log.trace("Sending event '{}' to {} emitter(s), data: '{}'", name, emitterCount, data);
        if (emitterCount == 0) {
            log.trace("No active emitters, skipping event '{}'", name);
            return;
        }
        for (ResponseBodyEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(SseEmitter.event()
                        .name(name)
                        .data(data)
                        .id(UUID.randomUUID().toString())
                        .build()
                );
                log.trace("Event '{}' sent successfully", name);
            } catch (IOException e) {
                log.warn("Exception while sending event '{}' to client, removing emitter", name, e);
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    SseEmitter createEmitter(long emitterTimeout) {
        return new SseEmitter(emitterTimeout);
    }

}
