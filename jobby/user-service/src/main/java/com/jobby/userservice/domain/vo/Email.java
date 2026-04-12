package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String email;
    private static final String REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$\n";

    public Result<Void, Error> validate(){
        return ValidationChain.create().validateNotBlank(this.getEmail(), "email")
                .validateIf(this.getEmail().matches(REGEX)
                , () -> Result.failure(ErrorType.VALIDATION_ERROR,
                                new Field("email", "The email address is invalid.")))
                .build();
    }
}
