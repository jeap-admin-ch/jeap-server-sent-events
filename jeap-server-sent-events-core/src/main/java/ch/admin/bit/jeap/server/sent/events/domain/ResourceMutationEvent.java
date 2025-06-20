package ch.admin.bit.jeap.server.sent.events.domain;

public record ResourceMutationEvent(String sendingApplication,
                                    ResourceMutationType type,
                                    String resourcePath) {
}
