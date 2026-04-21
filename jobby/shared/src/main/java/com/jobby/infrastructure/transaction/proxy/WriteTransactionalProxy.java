package com.jobby.infrastructure.transaction.proxy;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface WriteTransactionalProxy {
    Result<Void, Error> handle(Runnable runnable);
}
