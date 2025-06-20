package ch.admin.bit.jeap.server.sent.events.messaging;

public class NotifyClientKafkaException extends RuntimeException {

    private NotifyClientKafkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public static NotifyClientKafkaException producingCommandFailed(Exception cause) {
        return new NotifyClientKafkaException("Failed to produce event", cause);
    }
}
