package com.jobby.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface EmailService {
    Result<Void, Error> send(String from,
                             String to,
                             String subject,
                             String message);
}