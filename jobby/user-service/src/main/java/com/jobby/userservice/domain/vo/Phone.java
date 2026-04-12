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
public class Phone {
    private String number;
    private static final String REGEX = "^3\\d{9}$";
    private static final String PREFIX = "+57";

    public Result<Void, Error> validate(){
        return ValidationChain.create()
                .validateNotBlank(this.getNumber(), "phone")
                .validateIf(this.getNumber().matches(REGEX),
                        () -> Result.failure(ErrorType.VALIDATION_ERROR, new Field("phone", "the provided phone is invalid")))
                .build();
    }

    public String getNumberAsE164(){
        return PREFIX + " " + this.getNumber();
    }

    public String getNumberAsDisplay(){
        return "(" + PREFIX + ") " + this.getNumber();
    }

}
