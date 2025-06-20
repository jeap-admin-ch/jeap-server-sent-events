package ch.admin.bit.jeap.server.sent.events.domain;

public interface ResourceMutationEventListener {

    void onResourceMutation(ResourceMutationType type, String resourcePath);
}
