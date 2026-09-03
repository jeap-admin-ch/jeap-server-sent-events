package ch.admin.bit.jeap.server.sent.events.messaging;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.TopicPartitionOffset;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class NotifyClientPartitionMonitor implements SmartLifecycle {

    static final String LISTENER_ID_SUFFIX = "-sse-notify-client";

    private final String applicationName;
    private final String topic;
    private final long refreshRateInMs;
    private final NotifyClientPartitionFinder partitionFinder;
    private final NotifyClientCommandConsumer commandConsumer;
    private final KafkaListenerEndpointRegistry listenerEndpointRegistry;
    private final ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory;
    private final RecordFilterStrategy<Object, Object> recordFilterStrategy;
    private final Map<Integer, ConcurrentMessageListenerContainer<Object, Object>> additionalContainers =
            new ConcurrentHashMap<>();

    private volatile ScheduledExecutorService scheduler;
    private volatile boolean running;

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sse-kafka-partition-monitor");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::refreshPartitionsSafely,
                refreshRateInMs, refreshRateInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        running = false;
        ScheduledExecutorService currentScheduler = scheduler;
        if (currentScheduler != null) {
            currentScheduler.shutdownNow();
            scheduler = null;
        }
        additionalContainers.values().forEach(container -> {
            container.stop();
            container.destroy();
        });
        additionalContainers.clear();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    synchronized void refreshPartitions() {
        MessageListenerContainer primaryContainer = listenerEndpointRegistry.getListenerContainer(listenerId());
        if (primaryContainer == null) {
            log.warn("SSE Kafka listener {} is not available; partition refresh will be retried", listenerId());
            return;
        }

        Set<Integer> consumedPartitions = Arrays.stream(primaryContainer.getContainerProperties().getTopicPartitions())
                .map(TopicPartitionOffset::getPartition)
                .collect(Collectors.toSet());
        consumedPartitions.addAll(additionalContainers.keySet());

        Arrays.stream(partitionFinder.partitions(topic))
                .map(Integer::parseInt)
                .filter(partition -> !consumedPartitions.contains(partition))
                .sorted()
                .forEach(this::startAdditionalContainer);
    }

    boolean consumesPartitionWithoutGroup(int partition) {
        ConcurrentMessageListenerContainer<Object, Object> container = additionalContainers.get(partition);
        return container != null && container.getGroupId() == null;
    }

    private void startAdditionalContainer(int partition) {
        TopicPartitionOffset assignment =
                new TopicPartitionOffset(topic, partition, TopicPartitionOffset.SeekPosition.END);
        ConcurrentMessageListenerContainer<Object, Object> container = containerFactory.createContainer(assignment);
        container.setBeanName(listenerId() + "-partition-" + partition);
        container.getContainerProperties().setGroupId(null);
        MessageListener<Object, Object> listener = record ->
                commandConsumer.consume((NotifyClientCommand) record.value());
        container.setupMessageListener(new FilteringMessageListenerAdapter<>(listener, recordFilterStrategy, false));

        try {
            container.start();
            additionalContainers.put(partition, container);
            log.info("Started SSE consumer for newly added partition {} of topic {}", partition, topic);
        } catch (RuntimeException e) {
            container.destroy();
            throw e;
        }
    }

    private void refreshPartitionsSafely() {
        synchronized (this) {
            if (!running) {
                return;
            }
            try {
                refreshPartitions();
            } catch (RuntimeException e) {
                if (running && !Thread.currentThread().isInterrupted()) {
                    log.warn("Failed to refresh SSE Kafka partitions; retrying after {} ms", refreshRateInMs, e);
                }
            }
        }
    }

    private String listenerId() {
        return applicationName + LISTENER_ID_SUFFIX;
    }
}
