package ch.admin.bit.jeap.server.sent.events.domain;

public interface ResourceMutationListener {

    void onResourceMutation(String applicationName, ResourceMutationType type, String resourcePath, String processId);
}
