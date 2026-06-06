package com.jobby.userservice.domain.ports.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.vo.shared.Email;

public interface EmailChangeCodeService {
    Result<String, Error> next(long userId, Email newEmail);
    Result<Email, Error> validate(long userId, String code);
}
