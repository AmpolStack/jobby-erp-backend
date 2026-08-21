package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.domain.ports.in.CommandHandler;
import com.jobby.userservice.application.contracts.commands.UpdateUserStatusCommand;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateUserStatusUseCase implements CommandHandler<UpdateUserStatusCommand, Boolean> {

    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;

    @Override
    public Result<Boolean, Error> execute(UpdateUserStatusCommand command) {
        return this.userRepository.getById(command.userId())
                .flatMap(user -> {
                    user.updateStatus(command.isActive());
                    return this.userRepository.prepareSave(user);
                })
                .flatMap(task -> this.transaction.write()
                        .add(task)
                        .build())
                .map(v -> command.isActive());
    }
}
