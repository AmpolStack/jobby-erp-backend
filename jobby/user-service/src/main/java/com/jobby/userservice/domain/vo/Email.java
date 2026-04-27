package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email {
    private String email;
    private static final String REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String FIELD_NAME = "email address";

    public static Result<Email, Error> of(String email){
        return ValidationChain.create()
                .validateNotBlank(email, FIELD_NAME)
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateIf(!email.matches(REGEX),
                                () -> Result.failure(ErrorType.VALIDATION_ERROR,
                                        new Field("email", "The email address is invalid.")))
                        .build())
                .map(v -> new Email(email));
    }

    public static Email on(String email){
        return new Email(email);
    }
}
