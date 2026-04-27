package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.IdentificationType;
import lombok.*;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentificationNumber {
    private String number;
    private static final String NUMBER_FIELD_NAME = "identification number";
    private static final String TYPE_FIELD_NAME = "identification type";

    public static Result<IdentificationNumber, Error> of(String number,
                                                         IdentificationType type){
        return ValidationChain.create()
                .validateNotBlank(number, NUMBER_FIELD_NAME)
                .validateInternalNotNull(type, TYPE_FIELD_NAME)
                .build()
                .flatMap(v -> {
                    var cleanedNumber = number.trim();
                    return ValidationChain.create()
                            .validateGreaterOrEqualsThan(cleanedNumber.length(), type.getMinLength(),
                                    NUMBER_FIELD_NAME + " (" + type.getAbbreviation() + ")")
                            .validateSmallerOrEqualsThan(cleanedNumber.length(), type.getMaxLength(),
                                    NUMBER_FIELD_NAME + " (" + type.getAbbreviation() + ")")
                            .add(ValidExpression(cleanedNumber,
                                    type.getExpression(),
                                    type.getAllowCharacters()))
                            .build()
                            .map(v2 -> new IdentificationNumber(number));
                });
    }

    public static IdentificationNumber on(String number){
        return new IdentificationNumber(number);
    }

    private static Result<Void, Error> ValidExpression(String number,
                                                       String regex,
                                                       Set<String> allowCharacters) {
        if(number.matches(regex)){
            return Result.success(null);
        }

        var allowCharactersString = String.join(", ", allowCharacters);

        return Result.failure(ErrorType.VALIDATION_ERROR,
                new Field(NUMBER_FIELD_NAME, "The format is invalid. It can only contain: " + allowCharactersString));

    }

}

