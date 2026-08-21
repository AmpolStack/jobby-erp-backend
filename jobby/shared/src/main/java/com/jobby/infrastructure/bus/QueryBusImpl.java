package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Query;
import com.jobby.domain.ports.in.QueryBus;
import com.jobby.domain.ports.in.QueryHandler;

import java.util.HashMap;
import java.util.Map;

public class QueryBusImpl implements QueryBus {

    private final Map<Class<? extends Query>, QueryHandler<?, ?>> handlers = new HashMap<>();

    public <TQuery extends Query, TResponse> QueryBusImpl register(
            Class<TQuery> queryType, QueryHandler<TQuery, TResponse> handler) {
        handlers.put(queryType, handler);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Query query) {
        var handler = handlers.get(query.getClass());
        if (handler == null) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("query", "No handler registered for: " + query.getClass().getSimpleName()));
        }
        return ((QueryHandler<Query, TResponse>) handler).execute(query);
    }
}
