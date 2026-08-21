package com.jobby.infrastructure.transaction;

import com.jobby.ResultAssertions;
import com.jobby.domain.functional.PersistenceTask;
import com.jobby.ResultAssertions;
import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionOrchestratorAdapter - Unit Tests")
class TransactionOrchestratorAdapterTest {

    private SpringDataTransactionalContext stubContext;
    private PersistenceProxy happyProxy;
    private TransactionOrchestratorAdapter adapter;

    @BeforeEach
    void setUp() {
        stubContext = new SpringDataTransactionalContext() {
            @Override
            public <E> Result<E, Error> run(Supplier<Result<E, Error>> supplier) {
                return supplier.get();
            }

            @Override
            public <E> Result<E, Error> runReadOnly(Supplier<Result<E, Error>> supplier) {
                return supplier.get();
            }
        };
        happyProxy = new PersistenceProxy() {
            @Override
            public <T> Result<T, Error> read(Supplier<T> supplier) {
                try {
                    return Result.success(supplier.get());
                } catch (Exception e) {
                    return Result.failure(ErrorType.VALIDATION_ERROR, new Field("error", e.getMessage()));
                }
            }

            @Override
            public <T> Result<T, Error> write(Supplier<T> supplier) {
                try {
                    return Result.success(supplier.get());
                } catch (Exception e) {
                    return Result.failure(ErrorType.VALIDATION_ERROR, new Field("error", e.getMessage()));
                }
            }

            @Override
            public Result<Void, Error> write(Runnable runnable) {
                return write(() -> {
                    runnable.run();
                    return null;
                });
            }
        };
        adapter = new TransactionOrchestratorAdapter(stubContext, happyProxy);
    }

    @Test
    @DisplayName("read: success supplier returns data")
    void givenSuccessSupplier_whenRead_returnsData() {
        var result = adapter.read(() -> "hello world");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEqualTo("hello world");
    }

    @Test
    @DisplayName("read: null supplier result returns success with null")
    void givenNullSupplier_whenRead_returnsSuccessWithNull() {
        var result = adapter.read(() -> null);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isNull();
    }

    @Test
    @DisplayName("read: when proxy fails returns failure")
    void givenFailingProxy_whenRead_returnsFailure() {
        var failingProxy = new PersistenceProxy() {
            @Override
            public <T> Result<T, Error> read(Supplier<T> supplier) {
                return Result.failure(ErrorType.ITS_DB_CONNECTION_FAILED, new Field("db", "connection refused"));
            }

            @Override
            public <T> Result<T, Error> write(Supplier<T> supplier) {
                return happyProxy.write(supplier);
            }

            @Override
            public Result<Void, Error> write(Runnable runnable) {
                return happyProxy.write(runnable);
            }
        };
        var failingAdapter = new TransactionOrchestratorAdapter(stubContext, failingProxy);

        var result = failingAdapter.read(() -> "data");

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("write().build() with no operations returns success")
    void givenNoOperations_whenBuild_returnsSuccess() {
        var result = adapter.write().build();

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("write().add(Runnable).build() executes runnable and returns success")
    void givenRunnable_whenBuild_returnsSuccess() {
        var executed = new AtomicInteger(0);

        var result = adapter.write()
                .add((Runnable) executed::incrementAndGet)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(executed.get()).isOne();
    }

    @Test
    @DisplayName("write().add(PersistenceTask).build() executes task and returns success")
    void givenPersistenceTask_whenBuild_returnsSuccess() {
        var executed = new AtomicInteger(0);
        PersistenceTask task = () -> executed.incrementAndGet();

        var result = adapter.write()
                .add(task)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(executed.get()).isOne();
    }

    @Test
    @DisplayName("multiple operations all succeed")
    void givenMultipleOperations_whenBuild_allAreExecuted() {
        var counter = new AtomicInteger(0);

        Runnable increment = counter::incrementAndGet;
        var result = adapter.write()
                .add(increment)
                .add(increment)
                .add(increment)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("failing operation returns failure and clears operations")
    void givenFailingOperation_whenBuild_returnsFailure() {
        var executed = new AtomicInteger(0);

        var result = adapter.write()
                .add((Runnable) () -> {
                    executed.incrementAndGet();
                    throw new RuntimeException("operation failed");
                })
                .add((Runnable) executed::incrementAndGet)
                .build();

        ResultAssertions.assertFailure(result);
        assertThat(executed.get()).isOne();
    }

    @Test
    @DisplayName("build clears operations list")
    void givenOperations_whenBuildCalledTwice_secondCallHasNoOperations() {
        var executed = new AtomicInteger(0);
        Runnable increment = executed::incrementAndGet;

        adapter.write()
                .add(increment)
                .build();

        var secondResult = adapter.write().build();

        ResultAssertions.assertSuccess(secondResult);
        assertThat(executed.get()).isOne();
    }

    @Test
    @DisplayName("write(): mix of runnable and persistence task")
    void givenMixedOperations_whenBuild_allExecute() {
        var executed = new AtomicInteger(0);
        Runnable increment = executed::incrementAndGet;

        var result = adapter.write()
                .add(increment)
                .add((Runnable) () -> executed.incrementAndGet())
                .add(increment)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(executed.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("write(): fluent api returns same executor instance")
    void givenFluentApi_whenAdd_returnsSameExecutor() {
        var executor = adapter.write();

        var same = executor.add((Runnable) () -> {});

        assertThat(same).isSameAs(executor);
    }
}
