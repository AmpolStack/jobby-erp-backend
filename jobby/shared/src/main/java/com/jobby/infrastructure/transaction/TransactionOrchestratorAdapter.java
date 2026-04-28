package com.jobby.infrastructure.transaction;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
@Component
public class TransactionOrchestratorAdapter implements TransactionOrchestrator {
    protected final SpringDataTransactionalContext context;
    protected final PersistenceProxy persistenceProxy;

    @Override
    public <R> Result<R, Error> read(Supplier<R> supplier){
        return persistenceProxy.read(() -> context.runReadOnly(() -> Result.success(supplier.get())))
                .flatMap(inner -> inner);
    }

    @Override
    public WritingTransactionExecutor write(){
        return new WritingTransactionExecutor();
    }

    public final class WritingTransactionExecutor implements WritingTransactionOrchestrator{
        private final List<Supplier<Result<Void, Error>>> operations = new ArrayList<>();

        @Override
        public WritingTransactionExecutor add(Runnable runnable){
            operations.add(() -> {
                runnable.run();
                return Result.success();
            });
            return this;
        }

        @Override
        public WritingTransactionExecutor add(PersistenceTask task){
            operations.add(() -> {
                task.execute();
                return Result.success();
            });
            return this;
        }

        @Override
        public Result<Void, Error> build(){
            Result<Result<Void, Error>, Error> result = persistenceProxy.write(() -> context.run(() -> {
                Result<Void, Error> opResult = Result.success();
                for (Supplier<Result<Void, Error>> operation : operations) {
                    opResult = operation.get();
                    if (opResult.isFailure()) break;
                }
                return opResult;
            }));

            operations.clear();
            return result.flatMap(inner -> inner);
        }
    }
}
