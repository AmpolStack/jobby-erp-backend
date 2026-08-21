package com.jobby.userservice.domain.ports.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.models.vo.shared.Name;

public interface EmailSenderService {
    Result<Void, Error> sendWelcomeEmail(Email to, Name name);
    Result<Void, Error> sendEmailChangeCode(String code, Email email);
    Result<Void, Error> sendWelcomeOwner(String name, Email email);
    Result<Void, Error> sendEmailCorrectChanged(String name, Email email);
}
