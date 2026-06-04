package com.jobby.infrastructure.transaction.proxy;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import java.util.function.Supplier;

public interface PersistenceProxy {
    <T> Result<T, Error> read(Supplier<T> supplier);
    <T> Result<T, Error> write(Supplier<T> supplier);
    Result<Void, Error> write(Runnable runnable);
}
