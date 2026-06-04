package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.ConfirmEmailChangeCommand;
import com.jobby.userservice.domain.contract.commands.RequestEmailChangeCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.ports.in.UpdateEmailUseCase;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.EmailChangeCodeService;
import com.jobby.userservice.domain.ports.out.services.EmailSenderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateEmailUseCaseAdapter implements UpdateEmailUseCase {

    private final EmailSenderService emailSenderService;
    private final EmailChangeCodeService emailChangeCodeService;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final ResponseMapper responseMapper;

    @Override
    public Result<Void, Error> request(RequestEmailChangeCommand command) {
        return this.userRepository.getById(command.userId())
                .flatMap(user -> Email.of(command.newEmail())
                        .flatMap(email -> this.emailChangeCodeService.next(command.userId(), email)
                                .flatMap(code -> this.emailSenderService.sendEmailChangeCode(code, email))));
    }

    @Override
    public Result<UserResponse, Error> confirm(ConfirmEmailChangeCommand command) {
        return this.userRepository.getById(command.id())
                .flatMap(user -> this.emailChangeCodeService.validate(command.id(), command.code())
                        .flatMap(user::updateEmail)
                        .flatMap(v -> this.userRepository.prepareSave(user))
                        .flatMap(userTask -> this.transaction.write()
                                .add(userTask)
                                .build())
                        .flatMap(v -> this.emailSenderService.sendEmailCorrectChanged(user.getFirstName().getValue(), user.getEmail()))
                        .map(v -> this.responseMapper.toResponse(user)));
    }
}
