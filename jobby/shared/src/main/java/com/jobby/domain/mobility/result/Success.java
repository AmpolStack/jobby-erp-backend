package com.jobby.domain.mobility.result;

public record Success<T, E>(T data) implements Result<T, E> {

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public E error() {
        return null;
    }

    @Override
    public String toString() {
        return "Success{" + "data=" + data + '}';
    }
}
