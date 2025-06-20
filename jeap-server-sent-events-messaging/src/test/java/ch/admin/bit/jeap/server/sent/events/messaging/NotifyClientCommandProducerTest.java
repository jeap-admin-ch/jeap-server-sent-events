package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.command.notify.client.NotifyClientCommandType;
import ch.admin.bit.jeap.messaging.kafka.properties.KafkaProperties;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyClientCommandProducerTest {

    private static final String TOPIC = "test-topic";
    private static final String SYSTEM = "test-system";

    private KafkaProperties kafkaProperties;
    private KafkaTemplate kafkaTemplate;

    private NotifyClientCommandProducer notifyClientCommandConsumer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(completableFuture);
        kafkaProperties = mock(KafkaProperties.class);
        when(kafkaProperties.getSystemName()).thenReturn(SYSTEM);
        notifyClientCommandConsumer = new NotifyClientCommandProducer(TOPIC, kafkaProperties, kafkaTemplate);
    }

    @Test
    void onResourceMutationCreated() {
        String applicationName = "testApplication";
        String resourcePath = "/test/resource";
        notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_CREATED, resourcePath, null);

        ArgumentCaptor<NotifyClientCommand> captor = ArgumentCaptor.forClass(NotifyClientCommand.class);

        verify(kafkaTemplate).send(eq(TOPIC), isNull(), captor.capture());

        NotifyClientCommand command = captor.getValue();
        assertEquals(SYSTEM, command.getPublisher().getSystem());
        assertEquals(applicationName, command.getPublisher().getService());
        assertNull(command.getProcessId());

        assertEquals(resourcePath, command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_CREATED, command.getPayload().getType());
    }

    @Test
    void onResourceMutationCreatedWithProcessId() {
        String applicationName = "testApplication";
        String resourcePath = "/test/resource";
        String processId = "process-123";
        notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_CREATED, resourcePath, processId);

        ArgumentCaptor<NotifyClientCommand> captor = ArgumentCaptor.forClass(NotifyClientCommand.class);

        verify(kafkaTemplate).send(eq(TOPIC), isNull(), captor.capture());

        NotifyClientCommand command = captor.getValue();
        assertEquals(SYSTEM, command.getPublisher().getSystem());
        assertEquals(applicationName, command.getPublisher().getService());
        assertEquals(processId, command.getProcessId());

        assertEquals(resourcePath, command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_CREATED, command.getPayload().getType());
    }

    @Test
    void onResourceMutationUpdated() {
        String applicationName = "testApplication";
        String resourcePath = "/test/resource";
        notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_UPDATED, resourcePath, null);

        ArgumentCaptor<NotifyClientCommand> captor = ArgumentCaptor.forClass(NotifyClientCommand.class);

        verify(kafkaTemplate).send(eq(TOPIC), isNull(), captor.capture());

        NotifyClientCommand command = captor.getValue();
        assertEquals(SYSTEM, command.getPublisher().getSystem());
        assertEquals(applicationName, command.getPublisher().getService());
        assertNull(command.getProcessId());

        assertEquals(resourcePath, command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_UPDATED, command.getPayload().getType());
    }

    @Test
    void onResourceMutationDeleted() {
        String applicationName = "testApplication";
        String resourcePath = "/test/resource";
        notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_DELETED, resourcePath, null);

        ArgumentCaptor<NotifyClientCommand> captor = ArgumentCaptor.forClass(NotifyClientCommand.class);

        verify(kafkaTemplate).send(eq(TOPIC), isNull(), captor.capture());

        NotifyClientCommand command = captor.getValue();
        assertEquals(SYSTEM, command.getPublisher().getSystem());
        assertEquals(applicationName, command.getPublisher().getService());
        assertNull(command.getProcessId());

        assertEquals(resourcePath, command.getReferences().getResourceReference().getResourcePath());
        assertEquals(NotifyClientCommandType.RESOURCE_DELETED, command.getPayload().getType());
    }

    @Test
    void init() {
        assertDoesNotThrow(() -> notifyClientCommandConsumer.init());
    }

    @Test
    void initWhenSystemNotSet() {
        when(kafkaProperties.getSystemName()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            notifyClientCommandConsumer.init();
        });
    }

    @Test
    void throwExceptionWhenInterrupted() throws ExecutionException, InterruptedException {
        kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(completableFuture);
        kafkaProperties = mock(KafkaProperties.class);
        when(kafkaProperties.getSystemName()).thenReturn(SYSTEM);
        notifyClientCommandConsumer = new NotifyClientCommandProducer(TOPIC, kafkaProperties, kafkaTemplate);

        String applicationName = "testApplication";
        String resourcePath = "/test/resource";

        when(completableFuture.get())
                .thenThrow(new InterruptedException("Test interruption"));

        assertThrows(NotifyClientKafkaException.class, () -> notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_DELETED, resourcePath, null));
    }

    @Test
    void throwExceptionWhenExecutionException() throws ExecutionException, InterruptedException {
        kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(completableFuture);
        kafkaProperties = mock(KafkaProperties.class);
        when(kafkaProperties.getSystemName()).thenReturn(SYSTEM);
        notifyClientCommandConsumer = new NotifyClientCommandProducer(TOPIC, kafkaProperties, kafkaTemplate);

        String applicationName = "testApplication";
        String resourcePath = "/test/resource";

        when(completableFuture.get())
                .thenThrow(new ExecutionException(new RuntimeException()));

        assertThrows(NotifyClientKafkaException.class, () -> notifyClientCommandConsumer.onResourceMutation(applicationName, ResourceMutationType.RESOURCE_DELETED, resourcePath, null));
    }


}
