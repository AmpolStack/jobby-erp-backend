package com.jobby.domain.mobility.result;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.exceptions.InconsistencyResultException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T, E> permits Success, Failure {

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    T data();

    E error();

    // --- Factory methods ---

    static <T, E> Result<T, E> success(T data) {
        return new Success<>(data);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    static <T> Result<T, Error> failure(ErrorType errorCode, Field[] fields) {
        return new Failure<>(new Error(errorCode, fields));
    }

    static <T> Result<T, Error> failure(ErrorType errorCode, Field field) {
        return new Failure<>(new Error(errorCode, new Field[] { field }));
    }

    // --- Propagation helpers ---

    static <T, U, E> Result<U, E> propagateFailure(Result<T, E> result) {
        return new Failure<>(result.error());
    }

    static <T, U> Result<U, Error> propagateFailure(Result<T, Error> result, String fieldName) {
        Error source = result.error();

        Field[] updatedFields = Arrays.stream(source.getFields())
                .map(f -> {
                    if (!source.getCode().name().startsWith("ITN_")) {
                        return new Field(fieldName, f.getReason());
                    }
                    return f;
                })
                .toArray(Field[]::new);

        return Result.failure(new Error(source.getCode(), updatedFields));
    }

    // --- Transformations ---

    default <U> Result<U, E> map(Function<T, U> fn) {
        if (this.isSuccess()) {
            return Result.success(fn.apply(this.data()));
        }
        return Result.failure(this.error());
    }

    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> fn) {
        if (this.isSuccess()) {
            return fn.apply(this.data());
        }
        return Result.failure(this.error());
    }

    default void fold(Consumer<T> onSuccess, Consumer<E> onFailure) {
        if (this.isSuccess()) {
            onSuccess.accept(this.data());
        } else {
            onFailure.accept(this.error());
        }
    }

    // Legacy
    static <T, U, E> Result<U, E> mapError(Result<T, E> result) {
        if (result.isFailure()) {
            return new Failure<>(result.error());
        }
        throw new InconsistencyResultException("The result is failure");
    }
}
