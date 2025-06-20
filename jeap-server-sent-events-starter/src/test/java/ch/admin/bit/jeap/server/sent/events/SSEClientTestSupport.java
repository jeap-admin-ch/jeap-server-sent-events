package ch.admin.bit.jeap.server.sent.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SSEClientTestSupport {

    static SSEClient createTestClient(String baseUrl, String resourcePath, int expectedEventCount) throws Exception {
        return createTestClient(baseUrl, resourcePath, expectedEventCount, null);
    }

    @SuppressWarnings("java:S2925")
    static SSEClient createTestClient(String baseUrl, String resourcePath, int expectedEventCount, String token) throws Exception {
        CountDownLatch latch = new CountDownLatch(expectedEventCount);

        List<String> receivedEventTypes = Collections.synchronizedList(new ArrayList<>());
        List<Map<String, String>> receivedDataPayloads = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger statusCode = new AtomicInteger();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(baseUrl + resourcePath))
                        .header("Accept", "text/event-stream");
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
                HttpRequest request = requestBuilder.build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                statusCode.set(response.statusCode());
                if (response.statusCode() != 200) {
                    for (int i = 0; i < expectedEventCount; i++) {
                        latch.countDown();
                    }
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    String currentEventType = null;
                    StringBuilder dataBuffer = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        log.info("line: {}", line);
                        if (line.startsWith("event:")) {
                            currentEventType = line.substring(6).trim();
                        } else if (line.startsWith("data:")) {
                            dataBuffer.append(line.substring(5).trim());
                        } else if (line.isEmpty() && dataBuffer.length() > 0) {
                            // End of SSE block
                            if (currentEventType != null) {
                                receivedEventTypes.add(currentEventType);
                            }

                            // Parse JSON data payload
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, String> payload = mapper.readValue(dataBuffer.toString(), new TypeReference<>() {
                            });
                            receivedDataPayloads.add(payload);

                            dataBuffer.setLength(0); // Clear buffer for next event
                            currentEventType = null;

                            latch.countDown();
                            if (latch.getCount() == 0) break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("SSE reading error", e);
            }
        });

        // Give the SSE connection time to initialize
        Thread.sleep(500);

        return new SSEClient(
                statusCode,
                receivedEventTypes,
                receivedDataPayloads,
                executor,
                future,
                latch
        );
    }

    @RequiredArgsConstructor
    public static class SSEClient {
        private final AtomicInteger statusCode;
        private final List<String> receivedEventTypes;
        private final List<Map<String, String>> receivedDataPayloads;
        private final ExecutorService executor;
        private final Future<?> future;
        private final CountDownLatch latch;

        public void waitFor(int seconds) throws InterruptedException {
            expectHttpStatusCode(seconds, 200);
        }

        public void expectHttpStatusCode(int seconds, int httpStatusCode) throws InterruptedException {
            // Wait for both events to be received
            assertThat(latch.await(seconds, TimeUnit.SECONDS)).isTrue();
            assertEquals(httpStatusCode, statusCode.get(), "Expected HTTP status code " + httpStatusCode + " but got " + statusCode.get());

            // Cleanup thread
            future.cancel(true);
            executor.shutdownNow();
        }

        public void expectEventTypes(String... values) {
            assertThat(receivedEventTypes).containsExactly(values);
        }

        public void expectPayloads(Map<String, String>... payloads) {
            assertThat(receivedDataPayloads).hasSize(payloads.length);
            for (int i = 0; i < payloads.length; i++) {
                assertThat(receivedDataPayloads.get(i)).isEqualTo(payloads[i]);
            }
        }
    }
}
