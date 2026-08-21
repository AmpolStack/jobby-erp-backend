package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.EventHandler;
import com.jobby.userservice.application.contracts.events.UserCreatedEvent;
import com.jobby.userservice.domain.ports.out.services.EmailSenderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendWelcomeEmailUseCaseAdapter implements EventHandler<UserCreatedEvent, Void> {

    private final EmailSenderService emailSenderService;

    @Override
    public Result<Void, Error> execute(UserCreatedEvent event) {
        return this.emailSenderService.sendWelcomeEmail(event.getEmail(), event.getName());
    }
}
