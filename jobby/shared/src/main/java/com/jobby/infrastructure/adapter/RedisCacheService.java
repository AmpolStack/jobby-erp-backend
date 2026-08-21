package com.jobby.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.CacheService;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import java.time.Duration;

@Slf4j
@AllArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ObservationRegistry observationRegistry;

    private static final Result<?, Error> REDIS_CONNECTION_FAILURE_RESULT =  Result.failure(
            ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
            new Field(
                    "redis",
                    "Error connection with redis"
            )
    );

    @Override
    public <T> Result<Void, Error> register(String key, T value, Duration ttl) {
        return ValidationChain.create()
                .validateInternalNotBlank(key, "cache-key")
                .build()
                .flatMap(x -> {
                    var observation = Observation.createNotStarted("redis.register", observationRegistry).start();
                    try{
                        redisTemplate.opsForValue().set(key, value, ttl);
                        observation.stop();
                        return Result.success(null);
                    }
                    catch(SerializationException e){
                        observation.error(e);
                        log.warn("[ITS_SERIALIZATION_ERROR] Redis serialization failed on register: key={}", key, e);
                        return Result.failure(
                                ErrorType.ITS_SERIALIZATION_ERROR,
                                new Field(
                                        "serialization",
                                        e.getClass().getSimpleName() + ": serialization failed, object is invalid to serialize"
                                )
                        );
                    }
                    catch(RedisConnectionFailureException | QueryTimeoutException e){
                        observation.error(e);
                        log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Redis connection failed on register: key={}", key, e);
                        return Result.propagateFailure(REDIS_CONNECTION_FAILURE_RESULT);
                    }
                });
    }

    @Override
    public <T> Result<T, Error> get(String key, Class<T> type) {
        return ValidationChain.create()
                .validateInternalNotBlank(key, "cache-key")
                .build()
                .flatMap(x -> {
                    var observation = Observation.createNotStarted("redis.get", observationRegistry).start();
                    Object value;
                    try{
                        value = redisTemplate.opsForValue().get(key);
                        observation.stop();
                    }
                    catch(SerializationException e){
                        observation.error(e);
                        log.warn("[ITS_SERIALIZATION_ERROR] Redis deserialization failed on get: key={}, type={}", key, type.getSimpleName(), e);
                        return Result.failure(ErrorType.ITS_SERIALIZATION_ERROR,
                                new Field(
                                        "deserialization",
                                        e.getClass().getSimpleName() + ": deserialization failed"
                                )
                        );
                    }
                    catch(RedisConnectionFailureException | QueryTimeoutException e){
                        observation.error(e);
                        log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Redis connection failed on get: key={}", key, e);
                        return Result.propagateFailure(REDIS_CONNECTION_FAILURE_RESULT);
                    }

                    try{
                        T response = objectMapper.convertValue(value, type);
                        return Result.success(response);
                    }
                    catch(IllegalArgumentException e){
                        log.error("[ITS_OPERATION_ERROR] Redis type cast failed: key={}, targetType={}", key, type.getSimpleName(), e);
                        return Result.failure(
                                ErrorType.ITS_OPERATION_ERROR,
                                new Field(
                                        "type cast",
                                        e.getClass().getSimpleName() + ": object is not assignable to type " + type.getSimpleName()
                                )
                        );
                    }
                });
    }

    @Override
    public Result<Void, Error> remove(String key) {
        return ValidationChain.create()
                .validateInternalNotBlank(key, "cache-key")
                .build()
                .flatMap(x -> {
                    var observation = Observation.createNotStarted("redis.remove", observationRegistry).start();
                    try{
                        redisTemplate.delete(key);
                        observation.stop();
                        return Result.success(null);
                    }
                    catch(RedisConnectionFailureException e){
                        observation.error(e);
                        log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Redis connection failed on remove: key={}", key, e);
                        return Result.propagateFailure(REDIS_CONNECTION_FAILURE_RESULT);
                    }
                });
    }
}
