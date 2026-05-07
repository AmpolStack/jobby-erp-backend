package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImage {

    private final byte[] content;
    private final String mimeType;
    private final String key;

    private static final long MAX_SIZE_MB = 3;
    private static final long MAX_SIZE = MAX_SIZE_MB * 1024 * 1024L;
    private static final Map<String,String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpeg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    public static Result<ProfileImage, Error> of(byte[] content,
                                                 String mimeType,
                                                 ImageStorageContext context) {
        return ValidationChain.create()
                .validateNotNull(content, "profile image")
                .validateNotNull(mimeType, "profile image mimetype")
                .validateInternalNotNull(context, "profile image context")
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateIf((content.length > MAX_SIZE),
                                () -> Result.failure(ErrorType.VALIDATION_ERROR, new Field("profile image",
                                        "The file exceeds the allowed size of " + MAX_SIZE + " MB")))
                        .validateIf(!ALLOWED_TYPES.containsKey(mimeType),
                                () -> Result.failure(ErrorType.VALIDATION_ERROR, new Field("profile image",
                                        "Unallowed file type: " + mimeType)))
                        .validateInternalNotBlank(context.getFilename(), "profile image filename")
                        .validateInternalNotBlank(context.getStorageRoute(), "profile image storage route")
                        .build()
                )
                .map(v -> {
                    var key = context.getStorageRoute() + context.getFilename() + ALLOWED_TYPES.get(mimeType);
                    return new ProfileImage(content, mimeType, key);
                });
    }

}
