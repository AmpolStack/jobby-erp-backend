package com.jobby.infrastructure.transaction;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.function.Supplier;

public class TransactionalContext {
    @Transactional
    public <E> Result<E, com.jobby.domain.mobility.error.Error> run(Supplier<Result<E, com.jobby.domain.mobility.error.Error>> supplier) {
        var response = supplier.get();

        if(response.isFailure()){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return response;
    }

    @Transactional(readOnly = true)
    public <E> Result<E, Error> runReadOnly(Supplier<Result<E, Error>> supplier){
        return supplier.get();
    }
}
