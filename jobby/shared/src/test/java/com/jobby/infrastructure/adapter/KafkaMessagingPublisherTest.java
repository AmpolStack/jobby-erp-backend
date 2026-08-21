package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("KafkaMessagingPublisher - Unit Tests")
@ExtendWith(MockitoExtension.class)
class KafkaMessagingPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObservationRegistry observationRegistry;

    @Mock
    private Observation observation;

    @Captor
    private ArgumentCaptor<ProducerRecord<String, Object>> producerRecordCaptor;

    private KafkaMessagingPublisher publisher;

    @SuppressWarnings("unchecked")
    private CompletableFuture<SendResult<String, Object>> completedFuture() {
        return CompletableFuture.completedFuture(mock(SendResult.class, RETURNS_DEEP_STUBS));
    }

    @BeforeEach
    void setUp() {
        when(observation.lowCardinalityKeyValue(any(), any())).thenReturn(observation);
        when(observation.start()).thenReturn(observation);
        publisher = new KafkaMessagingPublisher(kafkaTemplate, observationRegistry);
    }

    @Test
    @DisplayName("publish without key sends ProducerRecord with null key")
    void givenDataWithoutKey_whenPublish_sendsRecordWithNullKey() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var result = publisher.publish("topic", "value", 5);

            ResultAssertions.assertSuccess(result);
            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            assertThat(producerRecordCaptor.getValue().key()).isNull();
        }
    }

    @Test
    @DisplayName("publish with key sends via KafkaTemplate and returns success")
    void givenDataWithKey_whenPublish_returnsSuccess() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(future);

            var result = publisher.publish("topic", "key", "value", 5);

            ResultAssertions.assertSuccess(result);
            verify(kafkaTemplate).send("topic", "key", "value");
        }
    }

    @Test
    @DisplayName("publish with metadata adds headers to ProducerRecord")
    void givenDataWithMetadata_whenPublish_addsHeaders() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var metadata = Map.of("eventId", "123", "occurredOn", "2024-01-01");
            var result = publisher.publish("topic", "key", "value", metadata, 5);

            ResultAssertions.assertSuccess(result);
            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            var record = producerRecordCaptor.getValue();
            assertThat(record.topic()).isEqualTo("topic");
            assertThat(record.key()).isEqualTo("key");
            assertThat(record.value()).isEqualTo("value");
            assertThat(record.headers().toArray())
                    .extracting(Header::key)
                    .containsExactlyInAnyOrder("eventId", "occurredOn");
        }
    }

    @Test
    @DisplayName("publish with null metadata sends ProducerRecord without headers")
    void givenDataWithNullMetadata_whenPublish_sendsWithoutHeaders() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var result = publisher.publish("topic", "key", "value", null, 5);

            ResultAssertions.assertSuccess(result);
            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            assertThat(producerRecordCaptor.getValue().headers().toArray()).isEmpty();
        }
    }

    @Test
    @DisplayName("publish: TimeoutException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenTimeout_whenPublish_returnsExternalServiceFailure() throws InterruptedException, ExecutionException, TimeoutException {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = mock(CompletableFuture.class);
            doThrow(new TimeoutException()).when(future).get(anyLong(), any(TimeUnit.class));
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var result = publisher.publish("topic", "key", "value", Map.of(), 5);

            ResultAssertions.assertFailure(result, ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
            verify(observation).error(any(TimeoutException.class));
        }
    }

    @Test
    @DisplayName("publish: ExecutionException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenExecutionFailure_whenPublish_returnsExternalServiceFailure() throws InterruptedException, ExecutionException, TimeoutException {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = mock(CompletableFuture.class);
            doThrow(new ExecutionException(new RuntimeException("kafka error"))).when(future).get(anyLong(), any(TimeUnit.class));
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var result = publisher.publish("topic", "key", "value", Map.of(), 5);

            ResultAssertions.assertFailure(result, ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
            verify(observation).error(any(ExecutionException.class));
        }
    }

    @Test
    @DisplayName("publish: InterruptedException returns ITS_OPERATION_ERROR and interrupts current thread")
    void givenInterruption_whenPublish_returnsOperationError() throws InterruptedException, ExecutionException, TimeoutException {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = mock(CompletableFuture.class);
            doThrow(new InterruptedException()).when(future).get(anyLong(), any(TimeUnit.class));
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var result = publisher.publish("topic", "key", "value", Map.of(), 5);

            ResultAssertions.assertFailure(result, ErrorType.ITS_OPERATION_ERROR);
            verify(observation).error(any(InterruptedException.class));
        }
    }

    @Test
    @DisplayName("publishAsync without key delegates to key-based async")
    void givenDataWithoutKey_whenPublishAsync_callsAsyncWithNullKey() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            publisher.publishAsync("topic", "value");

            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            assertThat(producerRecordCaptor.getValue().key()).isNull();
        }
    }

    @Test
    @DisplayName("publishAsync with metadata adds headers to ProducerRecord")
    void givenDataWithMetadata_whenPublishAsync_addsHeaders() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            var metadata = Map.of("eventId", "123");
            publisher.publishAsync("topic", "key", "value", metadata);

            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            var record = producerRecordCaptor.getValue();
            assertThat(record.headers().toArray())
                    .extracting(Header::key)
                    .containsExactly("eventId");
            assertThat(record.headers().lastHeader("eventId").value())
                    .isEqualTo("123".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("publishAsync with null metadata sends ProducerRecord without headers")
    void givenDataWithNullMetadata_whenPublishAsync_sendsWithoutHeaders() {
        try (var mockedObservation = mockStatic(Observation.class)) {
            mockedObservation.when(() -> Observation.createNotStarted(anyString(), any(ObservationRegistry.class))).thenReturn(observation);
            var future = completedFuture();
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            publisher.publishAsync("topic", "key", "value", null);

            verify(kafkaTemplate).send(producerRecordCaptor.capture());
            assertThat(producerRecordCaptor.getValue().headers().toArray()).isEmpty();
        }
    }
}
