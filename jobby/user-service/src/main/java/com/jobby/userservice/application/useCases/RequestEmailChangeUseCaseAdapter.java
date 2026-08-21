package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.CommandHandler;
import com.jobby.userservice.application.contracts.commands.RequestEmailChangeCommand;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.EmailChangeCodeService;
import com.jobby.userservice.domain.ports.out.services.EmailSenderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RequestEmailChangeUseCaseAdapter implements CommandHandler<RequestEmailChangeCommand, Void> {

    private final UserRepository userRepository;
    private final EmailChangeCodeService emailChangeCodeService;
    private final EmailSenderService emailSenderService;

    @Override
    public Result<Void, Error> execute(RequestEmailChangeCommand command) {
        return this.userRepository.getById(command.userId())
                .flatMap(user -> Email.of(command.newEmail())
                        .flatMap(email -> this.emailChangeCodeService.next(command.userId(), email)
                                .flatMap(code -> this.emailSenderService.sendEmailChangeCode(code, email))));
    }
}
