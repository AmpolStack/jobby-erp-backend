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

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrl {
    private String value;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String URL_REGEX = "^https://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./%-]+)?$";

    public Result<Void, Error> validate() {
        return ValidationChain.create()
                .validateNotBlank(value, "image-url")
                .validateIf(!value.matches(URL_REGEX), () ->
                        Result.failure(ErrorType.VALIDATION_ERROR, new Field("image-url", "The provided URL does not correspond to valid origins"))
                )
                .validateIf(!hasValidExtension(), () ->
                        Result.failure(ErrorType.VALIDATION_ERROR, new Field("image-url",
                                "The file extension is not supported; only the following are accepted: " + String.join(", ", ALLOWED_EXTENSIONS)))
                )
                .build();
    }

    private boolean hasValidExtension() {
        int dotIndex = value.lastIndexOf('.');
        if (dotIndex == -1) return false;
        String extension = value.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

}
