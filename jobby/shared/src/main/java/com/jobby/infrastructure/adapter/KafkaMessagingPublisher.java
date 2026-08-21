package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.MessagingPublisher;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
public class KafkaMessagingPublisher implements MessagingPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    @Override
    public <T> Result<Void, Error> publish(String topicIdentifier, T data, int timeout) {
        return publish(topicIdentifier, null, data, null, timeout);
    }

    @Override
    public <T> void publishAsync(String topicIdentifier, T data) {
        publishAsync(topicIdentifier, null, data, null);
    }

    @Override
    public <T> Result<Void, Error> publish(String topicIdentifier, String key, T data, int timeout) {
        var observation = Observation.createNotStarted("kafka.publish", observationRegistry)
                .lowCardinalityKeyValue("topic", topicIdentifier)
                .start();
        try {
            var future = kafkaTemplate.send(topicIdentifier, key, data);

            future.get(timeout, TimeUnit.SECONDS);
            observation.stop();
            return Result.success(null);

        } catch (TimeoutException e) {
            observation.error(e);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Kafka publish timeout: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("Kafka", e.getClass().getSimpleName() + ": timeout in message sending"));
        } catch (ExecutionException e) {
            observation.error(e);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Kafka publish execution failed: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("Kafka", e.getClass().getSimpleName() + ": execution error in message sending"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            observation.error(e);
            log.error("[ITS_OPERATION_ERROR] Kafka publish interrupted: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("Kafka", e.getClass().getSimpleName() + ": interrupted operation"));
        }
    }

    @Override
    public <T> void publishAsync(String topicIdentifier, String key, T data) {
        publishAsync(topicIdentifier, key, data, null);
    }

    private static <T> ProducerRecord<String, Object> createRecord(String topic, String key, T data, Map<String, String> metadata) {
        var record = new ProducerRecord<String, Object>(topic, key, data);
        if (metadata != null) {
            metadata.forEach((k, v) -> record.headers().add(k, v.getBytes(StandardCharsets.UTF_8)));
        }
        return record;
    }

    @Override
    public <T> Result<Void, Error> publish(String topicIdentifier, String key, T data, Map<String, String> metadata, int timeout) {
        var observation = Observation.createNotStarted("kafka.publish", observationRegistry)
                .lowCardinalityKeyValue("topic", topicIdentifier)
                .start();
        try {
            var record = createRecord(topicIdentifier, key, data, metadata);
            var future = kafkaTemplate.send(record);
            future.get(timeout, TimeUnit.SECONDS);
            observation.stop();
            return Result.success(null);

        } catch (TimeoutException e) {
            observation.error(e);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Kafka publish timeout: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("Kafka", e.getClass().getSimpleName() + ": timeout in message sending"));
        } catch (ExecutionException e) {
            observation.error(e);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Kafka publish execution failed: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("Kafka", e.getClass().getSimpleName() + ": execution error in message sending"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            observation.error(e);
            log.error("[ITS_OPERATION_ERROR] Kafka publish interrupted: topic={}", topicIdentifier, e);
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("Kafka", e.getClass().getSimpleName() + ": interrupted operation"));
        }
    }

    @Override
    public <T> void publishAsync(String topicIdentifier, String key, T data, Map<String, String> metadata) {
        var observation = Observation.createNotStarted("kafka.publish-async", observationRegistry)
                .lowCardinalityKeyValue("topic", topicIdentifier)
                .start();

        var record = createRecord(topicIdentifier, key, data, metadata);
        var future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                observation.stop();
                log.info("Kafka message published: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                observation.error(ex);
                log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Kafka async publish failed: topic={}", topicIdentifier, ex);
            }
        });
    }
}
