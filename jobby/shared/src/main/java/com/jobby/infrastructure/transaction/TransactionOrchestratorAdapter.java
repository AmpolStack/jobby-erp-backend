package com.jobby.infrastructure.transaction;


import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.transaction.proxy.ReadTransactionalProxy;
import com.jobby.infrastructure.transaction.proxy.WriteTransactionalProxy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
@Component
public class TransactionExecutor {
    protected final SpringDataTransactionalContext context;

    public <R> Result<R, Error> read(Supplier<R> supplier,
                                     ReadTransactionalProxy proxy){
        return this.context.runReadOnly(()-> proxy.handle(supplier));
    }

    public WritingTransactionExecutor write(){
        return new WritingTransactionExecutor();
    }

    public final class WritingTransactionExecutor {
        private final List<Supplier<Result<Void, Error>>> operations = new ArrayList<>();

        public WritingTransactionExecutor add(Runnable runnable,
                                                 WriteTransactionalProxy proxy){
            Supplier<Result<Void, Error>> operation = () -> proxy.handle(runnable);
            operations.add(operation);
            return this;
        }

        public WritingTransactionExecutor add(Runnable runnable){

            Supplier<Result<Void, Error>> operation = ()-> {
                runnable.run();
                return Result.success();
            };

            operations.add(operation);
            return this;
        }

        public Result<Void, Error> build(){
            var response =  context.run(() -> {
                Result<Void,Error> result = Result.success();

                for (Supplier<Result<Void, Error>> operation : operations) {
                    result = operation.get();
                    if (result.isFailure()) break;
                }

                return result;
            });

            operations.clear();
            return response;
        }
    }
}
