package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.*;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUrl {
    private String value;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    // TODO: replace for https in production
    private static final String URL_REGEX =
            "^https?://(localhost|[\\w.-]+\\.[a-zA-Z]{2,})(:\\d+)?(/[\\w./%-]+)?$";

    public static Result<ImageUrl, Error> of(String value) {
        return ValidationChain.create()
                .validateNotBlank(value, "image-url")
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateIf(!value.matches(URL_REGEX), () ->
                                Result.failure(ErrorType.VALIDATION_ERROR,
                                        new Field("image-url", "The provided URL does not correspond to valid origins"))
                        )
                        .validateIf(!hasValidExtension(value), () ->
                                Result.failure(ErrorType.VALIDATION_ERROR, new Field("image-url",
                                        "The file extension is not supported; only the following are accepted: " + String.join(", ", ALLOWED_EXTENSIONS)))
                        )
                        .build()
                        .map(v2 -> new ImageUrl(value)));
    }

    public static ImageUrl reconstruct(String value){
        return new ImageUrl(value);
    }

    private static boolean hasValidExtension(String value) {
        int dotIndex = value.lastIndexOf('.');
        if (dotIndex == -1) return false;
        String extension = value.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}

