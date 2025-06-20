package ch.admin.bit.jeap.server.sent.events.web;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NotifyClientHeartbeatSender {
    private static final String HEARTBEAT_EVENT = "HEARTBEAT";
    private static final String HEARTBEAT_EVENT_DATA_TEMPLATE = "{ \"interval\": %s }";

    private final NotifyClientController notifyClientController;
    private final ScheduledExecutorService heartbeatScheduler;
    private final long rateInMs;

    public NotifyClientHeartbeatSender(NotifyClientController notifyClientController, long intervalInSeconds) {
        this(notifyClientController, Executors.newSingleThreadScheduledExecutor(), intervalInSeconds);
    }

    @PostConstruct
    public void init() {
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, rateInMs, rateInMs, TimeUnit.MILLISECONDS);
    }


    private void sendHeartbeats() {
        notifyClientController.sendEvent(HEARTBEAT_EVENT, HEARTBEAT_EVENT_DATA_TEMPLATE.formatted(rateInMs));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down");
        heartbeatScheduler.shutdown();
    }
}
