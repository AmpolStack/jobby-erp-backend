package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.ContactType;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactValue {
    private String value;
    private static final int MAX_LENGTH = 250;
    private static final String FIELD_NAME = "contact value";

    public static Result<ContactValue, Error> of(String value, ContactType type){
        return ValidationChain.create()
                .validateNotBlank(value, FIELD_NAME)
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateSmallerOrEqualsThan(value.length(), MAX_LENGTH, "contact")
                        .add(ValidExpression(value, type.getExpression(), type.getType()))
                        .build()
                        .map(v2 -> new ContactValue(value)));
    }

    public static ContactValue on(String value){
        return new ContactValue(value);
    }

    private static Result<Void, Error> ValidExpression(String value,
                                                       String regex,
                                                       String typeName) {
        return ValidationChain.create()
                .validateIf(!value.matches(regex),
                        () -> Result.failure(ErrorType.VALIDATION_ERROR,
                                new Field(FIELD_NAME, "The format it provides does not correspond to the " + typeName + " format")))
                .build();
    }

}
