package ch.admin.bit.jeap.server.sent.events.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyClientHeartbeatSenderTest {

    private static final long INTERVAL_IN_MS = 500;

    private NotifyClientController notifyClientController;
    private ScheduledExecutorService scheduledExecutorService;

    private NotifyClientHeartbeatSender notifyClientHeartbeatSender;


    @BeforeEach
    void setUp() {
        notifyClientController = mock(NotifyClientController.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        notifyClientHeartbeatSender = new NotifyClientHeartbeatSender(notifyClientController, scheduledExecutorService, INTERVAL_IN_MS);
    }

    @Test
    void sendHeartbeats() {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        notifyClientHeartbeatSender.init();

        verify(scheduledExecutorService).scheduleAtFixedRate(captor.capture(), eq(INTERVAL_IN_MS), eq(INTERVAL_IN_MS), eq(TimeUnit.MILLISECONDS));
        Runnable runnable = captor.getValue();
        runnable.run();

        verify(notifyClientController).sendEvent("HEARTBEAT", "{ \"interval\": " + INTERVAL_IN_MS + " }");
    }

    @Test
    void init() {
        notifyClientHeartbeatSender.init();

        verify(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), eq(INTERVAL_IN_MS), eq(INTERVAL_IN_MS), eq(TimeUnit.MILLISECONDS));
    }


    @Test
    void shutdown() {
        notifyClientHeartbeatSender.shutdown();

        verify(scheduledExecutorService).shutdown();
    }

}
