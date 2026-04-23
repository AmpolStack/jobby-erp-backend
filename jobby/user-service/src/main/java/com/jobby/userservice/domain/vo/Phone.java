package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Phone {
    private String number;
    private static final String REGEX = "^3\\d{9}$";
    private static final String PREFIX = "+57";
    private static final String FIELD_NAME = "phone number";

    public static Result<Phone, Error> of(String number){
        return ValidationChain.create()
                .validateNotBlank(number, FIELD_NAME)
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateIf(!number.matches(REGEX),
                                () -> Result.failure(ErrorType.VALIDATION_ERROR, new Field(FIELD_NAME, "the provided phone is invalid")))
                        .build())
                .map(v -> new Phone(number));
    }

    public static Phone on(String number){
        return new Phone(number);
    }

    public String getNumberAsE164(){
        return PREFIX + " " + this.getNumber();
    }
    public String getNumberAsDisplay(){
        return "(" + PREFIX + ") " + this.getNumber();
    }
}
