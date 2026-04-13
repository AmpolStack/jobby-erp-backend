package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.IdentificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IdentificationNumber {
    private String number;

    private static final String FIELD_NAME = "identification number";

    public Result<Void, Error> validate(IdentificationType type){
        return ValidationChain.create()
                .validateNotBlank(number, FIELD_NAME)
                .validateGreaterOrEqualsThan(number.length(), type.getMinLength(),
                        FIELD_NAME + " (" + type.getAbbreviation() + ")")
                .validateSmallerOrEqualsThan(number.length(), type.getMaxLength(),
                        FIELD_NAME + " (" + type.getAbbreviation() + ")")
                .add(ValidExpression(type.getExpression(), type.getAllowCharacters()))
                .build();
    }

    private Result<Void, Error> ValidExpression(String regex, Set<String> allowCharacters) {
        if(number.matches(regex)){
            return Result.success(null);
        }

        var allowCharactersString = String.join(", ", allowCharacters);

        return Result.failure(ErrorType.VALIDATION_ERROR,
                new Field(FIELD_NAME, "The format is invalid. It can only contain: " + allowCharactersString));

    }

}

