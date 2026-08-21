package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Command;
import com.jobby.domain.ports.in.CommandBus;
import com.jobby.domain.ports.in.CommandHandler;

import java.util.HashMap;
import java.util.Map;

public class CommandBusImpl implements CommandBus {

    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public <TCommand extends Command, TResponse> CommandBusImpl register(
            Class<TCommand> commandType, CommandHandler<TCommand, TResponse> handler) {
        handlers.put(commandType, handler);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Command command) {
        var handler = handlers.get(command.getClass());
        if (handler == null) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("command", "No handler registered for: " + command.getClass().getSimpleName()));
        }
        return ((CommandHandler<Command, TResponse>) handler).execute(command);
    }
}
