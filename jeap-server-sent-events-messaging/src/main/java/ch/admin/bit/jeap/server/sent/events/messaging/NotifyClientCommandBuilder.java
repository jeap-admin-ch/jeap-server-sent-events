package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.avro.AvroCommandBuilder;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandPayload;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandReferences;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandResourceReference;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;

import java.time.LocalDateTime;

public class NotifyClientCommandBuilder extends AvroCommandBuilder<NotifyClientCommandBuilder, NotifyClientCommand> {

    private final String serviceName;
    private final String systemName;
    private String resourcePath;
    private NotifyClientCommandType type;

    public static NotifyClientCommand buildCommand(String systemName, String serviceName, String resourcePath, NotifyClientCommandType type, String processId) {
        NotifyClientCommandBuilder builder = new NotifyClientCommandBuilder(serviceName, systemName);
        builder.setResourcePath(resourcePath);
        builder.setType(type);
        builder.setProcessId(processId);
        return builder.build();
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setType(NotifyClientCommandType type) {
        this.type = type;
    }


    private NotifyClientCommandBuilder(String serviceName, String systemName) {
        super(NotifyClientCommand::new);
        this.serviceName = serviceName;
        this.systemName = systemName;
    }

    @Override
    public NotifyClientCommand build() {
        idempotenceId(createIdempotenceId());
        NotifyClientCommandPayload payload = NotifyClientCommandPayload.newBuilder().setType(type).build();
        setPayload(payload);

        NotifyClientCommandReferences references = NotifyClientCommandReferences.newBuilder()
                .setResourceReference(NotifyClientCommandResourceReference.newBuilder()
                        .setResourcePath(resourcePath)
                        .build())
                .build();
        setReferences(references);

        return super.build();
    }

    private String createIdempotenceId() {
        return String.format("%s-%s-%s-%s-%s", systemName, serviceName, resourcePath, type.name(), LocalDateTime.now());
    }


    @Override
    protected String getServiceName() {
        return serviceName;
    }

    @Override
    protected String getSystemName() {
        return systemName;
    }

    @Override
    protected NotifyClientCommandBuilder self() {
        return this;
    }
}
