package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.ContactType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactValue {
    private String value;

    private static final String FIELD_NAME = "contact value";

    public Result<Void, Error> validate(ContactType type){
        return ValidationChain.create()
                .validateNotBlank(value, FIELD_NAME)
                .add(ValidExpression(type.getExpression(), type.getType()))
                .build();
    }

    private Result<Void, Error> ValidExpression(String regex, String typeName) {
        if(value.matches(regex)){
            return Result.success(null);
        }

        return Result.failure(ErrorType.VALIDATION_ERROR,
                new Field(FIELD_NAME, "The format it provides does not correspond to the" + typeName + " format"));

    }
}
