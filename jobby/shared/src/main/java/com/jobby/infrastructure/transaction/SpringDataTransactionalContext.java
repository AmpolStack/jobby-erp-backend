package com.jobby.infrastructure.transaction;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.function.Supplier;

public class SpringDataTransactionalContext {

    private static final Logger log = LoggerFactory.getLogger(SpringDataTransactionalContext.class);

    @Transactional
    public <E> Result<E, Error> run(Supplier<Result<E, Error>> supplier) {
        var response = supplier.get();

        if(response.isFailure()){
            log.warn("Transaction rollback triggered: code={}, fields={}",
                    response.error().getCode(), (Object[]) response.error().getFields());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return response;
    }

    @Transactional(readOnly = true)
    public <E> Result<E, Error> runReadOnly(Supplier<Result<E, Error>> supplier){
        return supplier.get();
    }
}
