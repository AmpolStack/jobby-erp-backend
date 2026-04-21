package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Name {
    private String value;
    private static final int MAX_LENGTH = 150;

    public static Result<Name, Error> of(String value, String fieldName){
        return ValidationChain.create()
                .validateNotBlank(value, fieldName)
                .build()
                .flatMap(v -> {
                    var cleanValue = value.trim();
                    return ValidationChain.create()
                                    .validateSmallerOrEqualsThan(cleanValue.length(), MAX_LENGTH, fieldName)
                                    .build()
                            .map(v2 -> new Name(cleanValue));
                });
    }

    public static Name on(String value){
        return new Name(value);
    }
}
