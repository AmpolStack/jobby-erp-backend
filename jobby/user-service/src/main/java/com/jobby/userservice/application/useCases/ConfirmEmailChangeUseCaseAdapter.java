package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.CommandHandler;
import com.jobby.userservice.application.contracts.commands.ConfirmEmailChangeCommand;
import com.jobby.userservice.application.responses.UserResponse;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.EmailChangeCodeService;
import com.jobby.userservice.domain.ports.out.services.EmailSenderService;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.domain.ports.TransactionOrchestrator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConfirmEmailChangeUseCaseAdapter implements CommandHandler<ConfirmEmailChangeCommand, UserResponse> {

    private final UserRepository userRepository;
    private final EmailChangeCodeService emailChangeCodeService;
    private final EmailSenderService emailSenderService;
    private final TransactionOrchestrator transaction;
    private final ResponseMapper responseMapper;

    @Override
    public Result<UserResponse, Error> execute(ConfirmEmailChangeCommand command) {
        return this.userRepository.getById(command.id())
                .flatMap(user -> this.emailChangeCodeService.validate(command.id(), command.code())
                        .flatMap(user::updateEmail)
                        .flatMap(v -> this.userRepository.prepareSave(user))
                        .flatMap(userTask -> this.transaction.write()
                                .add(userTask)
                                .build())
                        .flatMap(v -> this.emailSenderService.sendEmailCorrectChanged(
                                user.getFirstName().getValue(), user.getEmail()))
                        .map(v -> this.responseMapper.toResponse(user)));
    }
}
