package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.EmailService;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.ports.out.services.EmailSenderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.Year;

@Service
@AllArgsConstructor
public class EmailSenderServiceAdapter implements EmailSenderService {

    private final EmailService emailService;
    private final TemplateLoader templateLoader;

    private static final String ORIGIN_EMAIL = "noreply+support@jobby.com";
    private static final int EXPIRATION_TIME = 5;

    @Override
    public Result<Void, Error> sendWelcomeEmail() {
        return null;
    }

    @Override
    public Result<Void, Error> sendEmailChangeCode(String code, Email email) {
        return this.templateLoader.load("email-change.html")
                .flatMap(content -> {

                    content = content.replace("{{code}}", code)
                            .replace("{{minutes}}", Integer.toString(EXPIRATION_TIME))
                            .replace("{{year}}", String.valueOf(Year.now().getValue()));

                    return this.emailService
                            .send(ORIGIN_EMAIL,
                                    email.getEmail(),
                                    "Confirm your new email address — Jobby",
                                    content);
                });
    }

    @Override
    public Result<Void, Error> sendWelcomeOwner(String name, Email email) {
        return this.templateLoader.load("welcome-owner.html")
                .flatMap(content -> {

                    content = content.replace("{{name}}", name)
                            .replace("{{year}}", String.valueOf(Year.now().getValue()));

                    return this.emailService
                            .send(ORIGIN_EMAIL,
                                    email.getEmail(),
                                    "Welcome to Jobby — let's get started",
                                    content);
                });
    }

    @Override
    public Result<Void, Error> sendEmailCorrectChanged(String name, Email email) {
        return this.templateLoader.load("email-correct-changed.html")
                .flatMap(content -> {

                    content = content.replace("{{name}}", name)
                            .replace("{{datetime}}", Instant.now().toString())
                            .replace("{{year}}", String.valueOf(Year.now().getValue()));

                    return this.emailService
                            .send(ORIGIN_EMAIL,
                                    email.getEmail(),
                                    "Your email address has been updated — Jobby",
                                    content);
                });
    }
}
