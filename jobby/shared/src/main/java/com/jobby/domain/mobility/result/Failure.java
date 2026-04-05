package com.jobby.domain.mobility.result;

public record Failure<T, E>(E error) implements Result<T, E> {

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public T data() {
        return null;
    }

    @Override
    public String toString() {
        return "Failure{" + "error=" + error.toString() + '}';
    }
}
