package com.jobby.infrastructure.transaction.proxy;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.springframework.dao.*;
import org.springframework.data.mongodb.MongoTransactionException;
import java.util.function.Supplier;

public class MongoDbSpringDataHandler implements WriteTransactionalProxy, ReadTransactionalProxy {
    @Override
    public <T> Result<T, Error> handle(Supplier<T> supplier) {
        try {
            var result = supplier.get();
            return Result.success(result);
        } catch (QueryTimeoutException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("timeout", "Query execution exceeded maximum allowed time"));
        } catch (TransientDataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("transient", "Transient MongoDB access issue, please retry"));
        } catch (NonTransientDataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("non-transient", "Non-transient MongoDB data access issue"));
        } catch (DataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("data access", "Generic MongoDB data access error"));
        } catch (Exception ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("unexpected", "Unexpected error: " + ex.getMessage()));
        }
    }

    @Override
    public Result<Void, Error> handle(Runnable runnable) {
        try {
            runnable.run();
            return Result.success();
        } catch (DuplicateKeyException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("duplicate key", "Duplicate key or unique constraint violation"));
        } catch (DataIntegrityViolationException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("data integrity", "Data integrity or constraint violation"));
        } catch (OptimisticLockingFailureException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("optimistic lock", "Optimistic locking failure: document modified by another transaction"));
        } catch (MongoTransactionException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("transaction", "Transaction failed or aborted unexpectedly"));
        } catch (RecoverableDataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("resource", "MongoDB resource unavailable or network failure"));
        } catch (TransientDataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("transient", "Transient MongoDB access issue, please retry"));
        } catch (NonTransientDataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("non-transient", "Persistent MongoDB data access issue"));
        } catch (DataAccessException ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("data access", "Generic MongoDB data access error"));
        } catch (Exception ex) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("unexpected", "Unexpected error: " + ex.getMessage()));
        }
    }
}
