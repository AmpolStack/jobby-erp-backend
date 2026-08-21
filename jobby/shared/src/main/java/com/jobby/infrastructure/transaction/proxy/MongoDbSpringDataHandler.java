package com.jobby.infrastructure.transaction.proxy;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.*;
import org.springframework.data.mongodb.MongoTransactionException;
import java.util.function.Supplier;

public class MongoDbSpringDataHandler implements PersistenceProxy {

    private static final Logger log = LoggerFactory.getLogger(MongoDbSpringDataHandler.class);
    private final ObservationRegistry observationRegistry;

    public MongoDbSpringDataHandler(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public <T> Result<T, Error> read(Supplier<T> supplier) {
        var observation = Observation.createNotStarted("mongodb.read", observationRegistry).start();
        try {
            var result = supplier.get();
            observation.stop();
            return Result.success(result);
        } catch (QueryTimeoutException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] MongoDB read timeout", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("timeout", ex.getClass().getSimpleName() + ": query timed out"));
        } catch (TransientDataAccessException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Transient MongoDB read error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("transient", ex.getClass().getSimpleName() + ": transient MongoDB access issue, retry"));
        } catch (NonTransientDataAccessException ex) {
            observation.error(ex);
            log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Non-transient MongoDB read error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("non-transient", ex.getClass().getSimpleName() + ": non-transient MongoDB data access issue"));
        } catch (DataAccessException ex) {
            observation.error(ex);
            log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Unexpected MongoDB read error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("data access", ex.getClass().getSimpleName() + ": generic MongoDB data access error"));
        } catch (Exception ex) {
            observation.error(ex);
            log.error("[ITS_OPERATION_ERROR] Unexpected error during read", ex);
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("unexpected", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    @Override
    public <T> Result<T, Error> write(Supplier<T> supplier) {
        var observation = Observation.createNotStarted("mongodb.write", observationRegistry).start();
        try {
            var result = supplier.get();
            observation.stop();
            return Result.success(result);
        } catch (DuplicateKeyException ex) {
            observation.error(ex);
            log.debug("[VALIDATION_ERROR] Duplicate key violation", ex);
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("duplicate key", ex.getClass().getSimpleName() + ": duplicate key or unique constraint violation"));
        } catch (DataIntegrityViolationException ex) {
            observation.error(ex);
            log.debug("[VALIDATION_ERROR] Data integrity violation", ex);
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("data integrity", ex.getClass().getSimpleName() + ": data integrity or constraint violation"));
        } catch (OptimisticLockingFailureException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Optimistic locking conflict", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("optimistic lock", ex.getClass().getSimpleName() + ": document modified by another transaction"));
        } catch (MongoTransactionException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] MongoDB transaction failed", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("transaction", ex.getClass().getSimpleName() + ": transaction failed or aborted"));
        } catch (RecoverableDataAccessException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] MongoDB resource unavailable", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("resource", ex.getClass().getSimpleName() + ": MongoDB resource unavailable or network failure"));
        } catch (TransientDataAccessException ex) {
            observation.error(ex);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Transient MongoDB write error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("transient", ex.getClass().getSimpleName() + ": transient MongoDB access issue, retry"));
        } catch (NonTransientDataAccessException ex) {
            observation.error(ex);
            log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Non-transient MongoDB write error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("non-transient", ex.getClass().getSimpleName() + ": persistent MongoDB data access issue"));
        } catch (DataAccessException ex) {
            observation.error(ex);
            log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Unexpected MongoDB write error", ex);
            return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                    new Field("data access", ex.getClass().getSimpleName() + ": generic MongoDB data access error"));
        } catch (Exception ex) {
            observation.error(ex);
            log.error("[ITS_OPERATION_ERROR] Unexpected error during write", ex);
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("unexpected", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    @Override
    public Result<Void, Error> write(Runnable runnable) {
        return write(() -> {
            runnable.run();
            return null;
        });
    }
}
