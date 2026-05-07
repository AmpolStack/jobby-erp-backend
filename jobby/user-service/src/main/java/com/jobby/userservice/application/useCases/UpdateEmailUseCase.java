package com.jobby.userservice.application.useCases;

import com.jobby.domain.ports.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateEmailUseCase {

    private final EmailService service;


}
