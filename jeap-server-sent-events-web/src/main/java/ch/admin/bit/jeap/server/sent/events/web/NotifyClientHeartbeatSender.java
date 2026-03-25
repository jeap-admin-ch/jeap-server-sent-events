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

    public NotifyClientHeartbeatSender(NotifyClientController notifyClientController, long rateInMs) {
        this(notifyClientController, Executors.newSingleThreadScheduledExecutor(), rateInMs);
    }

    @PostConstruct
    public void init() {
        log.info("Scheduling heartbeat with interval {} ms", rateInMs);
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, rateInMs, rateInMs, TimeUnit.MILLISECONDS);
    }


    private void sendHeartbeats() {
        log.trace("Sending heartbeat");
        try {
            notifyClientController.sendEvent(HEARTBEAT_EVENT, HEARTBEAT_EVENT_DATA_TEMPLATE.formatted(rateInMs));
        } catch (Exception e) {
            log.warn("Exception while sending heartbeat", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down");
        heartbeatScheduler.shutdown();
    }
}
