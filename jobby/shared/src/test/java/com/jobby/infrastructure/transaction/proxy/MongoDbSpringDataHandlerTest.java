package com.jobby.infrastructure.transaction.proxy;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.mongodb.MongoTransactionException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MongoDbSpringDataHandler - Unit Tests")
class MongoDbSpringDataHandlerTest {

    private MongoDbSpringDataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MongoDbSpringDataHandler(null);
    }

    /* ────────────── READ ────────────── */

    @Test
    @DisplayName("read: success supplier returns success with data")
    void givenSuccessSupplier_whenRead_returnsSuccessWithData() {
        var result = handler.read(() -> "data");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEqualTo("data");
    }

    @Test
    @DisplayName("read: null result returns success with null")
    void givenNullSupplier_whenRead_returnsSuccessWithNull() {
        var result = handler.read(() -> null);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isNull();
    }

    @Test
    @DisplayName("read: QueryTimeoutException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenQueryTimeoutException_whenRead_returnsItsExternalServiceFailure() {
        var result = handler.<String>read(() -> { throw new QueryTimeoutException("timeout"); });

        assertItsExternalServiceErrorWithField(result, "timeout");
    }

    @Test
    @DisplayName("read: TransientDataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenTransientDataAccessException_whenRead_returnsItsExternalServiceFailure() {
        var result = handler.<String>read(() -> { throw new TransientDataAccessException("transient") {}; });

        assertItsExternalServiceErrorWithField(result, "transient");
    }

    @Test
    @DisplayName("read: NonTransientDataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenNonTransientDataAccessException_whenRead_returnsItsExternalServiceFailure() {
        var result = handler.<String>read(() -> { throw new NonTransientDataAccessException("non-transient") {}; });

        assertItsExternalServiceErrorWithField(result, "non-transient");
    }

    @Test
    @DisplayName("read: generic DataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenDataAccessException_whenRead_returnsItsExternalServiceFailure() {
        var result = handler.<String>read(() -> { throw new DataAccessException("generic") {}; });

        assertItsExternalServiceErrorWithField(result, "data access");
    }

    @Test
    @DisplayName("read: generic Exception returns ITS_OPERATION_ERROR")
    void givenGenericException_whenRead_returnsItsOperationError() {
        var result = handler.<String>read(() -> { throw new RuntimeException("unexpected error"); });

        assertItsOperationErrorWithField(result, "unexpected");
    }

    /* ────────────── WRITE Supplier ────────────── */

    @Test
    @DisplayName("write: success supplier returns success with data")
    void givenSuccessSupplier_whenWrite_returnsSuccessWithData() {
        var result = handler.write(() -> "value");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEqualTo("value");
    }

    @Test
    @DisplayName("write: DuplicateKeyException returns VALIDATION_ERROR")
    void givenDuplicateKeyException_whenWrite_returnsValidationError() {
        var result = handler.write(() -> { throw new DuplicateKeyException("duplicate"); });

        assertValidationErrorWithField(result, "duplicate key");
    }

    @Test
    @DisplayName("write: DataIntegrityViolationException returns VALIDATION_ERROR")
    void givenDataIntegrityViolationException_whenWrite_returnsValidationError() {
        var result = handler.write(() -> { throw new DataIntegrityViolationException("integrity"); });

        assertValidationErrorWithField(result, "data integrity");
    }

    @Test
    @DisplayName("write: OptimisticLockingFailureException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenOptimisticLockingFailureException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new OptimisticLockingFailureException("lock"); });

        assertItsExternalServiceErrorWithField(result, "optimistic lock");
    }

    @Test
    @DisplayName("write: MongoTransactionException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenMongoTransactionException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new MongoTransactionException("tx failed"); });

        assertItsExternalServiceErrorWithField(result, "transaction");
    }

    @Test
    @DisplayName("write: RecoverableDataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenRecoverableDataAccessException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new RecoverableDataAccessException("resource"); });

        assertItsExternalServiceErrorWithField(result, "resource");
    }

    @Test
    @DisplayName("write: TransientDataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenTransientDataAccessException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new TransientDataAccessException("transient") {}; });

        assertItsExternalServiceErrorWithField(result, "transient");
    }

    @Test
    @DisplayName("write: NonTransientDataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenNonTransientDataAccessException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new NonTransientDataAccessException("non-transient") {}; });

        assertItsExternalServiceErrorWithField(result, "non-transient");
    }

    @Test
    @DisplayName("write: generic DataAccessException returns ITS_EXTERNAL_SERVICE_FAILURE")
    void givenDataAccessException_whenWrite_returnsItsExternalServiceFailure() {
        var result = handler.write(() -> { throw new DataAccessException("generic") {}; });

        assertItsExternalServiceErrorWithField(result, "data access");
    }

    @Test
    @DisplayName("write: generic Exception returns ITS_OPERATION_ERROR")
    void givenGenericException_whenWrite_returnsItsOperationError() {
        var result = handler.write(() -> { throw new RuntimeException("unexpected"); });

        assertItsOperationErrorWithField(result, "unexpected");
    }

    /* ────────────── WRITE Runnable ────────────── */

    @Test
    @DisplayName("write(Runnable): success runs and returns success")
    void givenSuccessRunnable_whenWrite_returnsSuccess() {
        var executed = new AtomicBoolean(false);

        var result = handler.write(() -> executed.set(true));

        ResultAssertions.assertSuccess(result);
        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("write(Runnable): supplier exception path still applies")
    void givenFailingRunnable_whenWrite_returnsFailure() {
        var result = handler.write(() -> { throw new DuplicateKeyException("dup"); });

        assertValidationErrorWithField(result, "duplicate key");
    }

    /* ────────────── helpers ────────────── */

    private static <T> void assertValidationErrorWithField(Result<T, Error> result, String expectedInstance) {
        ResultAssertions.assertFailure(result);
        assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
        assertThat(result.error().getFields())
                .isNotEmpty()
                .anySatisfy(field -> assertThat(field.getInstance()).isEqualTo(expectedInstance));
    }

    private static <T> void assertItsExternalServiceErrorWithField(Result<T, Error> result, String expectedInstance) {
        ResultAssertions.assertFailure(result);
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
        assertThat(result.error().getFields())
                .isNotEmpty()
                .anySatisfy(field -> assertThat(field.getInstance()).isEqualTo(expectedInstance));
    }

    private static <T> void assertItsOperationErrorWithField(Result<T, Error> result, String expectedInstance) {
        ResultAssertions.assertFailure(result);
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_OPERATION_ERROR);
        assertThat(result.error().getFields())
                .isNotEmpty()
                .anySatisfy(field -> assertThat(field.getInstance()).isEqualTo(expectedInstance));
    }
}
