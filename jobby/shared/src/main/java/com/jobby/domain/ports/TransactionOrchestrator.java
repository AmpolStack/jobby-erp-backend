package com.jobby.domain.ports;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import java.util.function.Supplier;

public interface TransactionOrchestrator {
    <R> Result<R,Error> read(Supplier<R> supplier);

    WritingTransactionOrchestrator write();

    interface WritingTransactionOrchestrator{
        WritingTransactionOrchestrator add(Runnable runnable);
        WritingTransactionOrchestrator add(PersistenceTask task);
        Result<Void, Error> build();
    }
}