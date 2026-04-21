package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

public final class CryptoUtils {

    private static final int BIT_MULTIPLIER = 8;


    // --- Base64 Operations ---

    public static Result<byte[], Error> decodeBase64(String encodedData, String fieldName) {
        return ValidationChain.create()
                .validateInternalNotBlank(encodedData, fieldName)
                .build()
                .flatMap(v -> {
                    try {
                        return Result.success(Base64.getDecoder().decode(encodedData));
                    } catch (IllegalArgumentException e) {
                        return Result.failure(
                                ErrorType.ITS_SERIALIZATION_ERROR,
                                new Field(fieldName, "Invalid Base64 encoded value")
                        );
                    }
                });
    }

    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    // --- Key Operations ---

    public static SecretKeySpec createKeySpec(byte[] keyBytes, String algorithm) {
        return new SecretKeySpec(keyBytes, algorithm);
    }

    public static SecretKeySpec parseKeySpec(String algorithm, String keyBase64) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(keyBase64);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new SecretKeySpec(keyBytes, algorithm);
    }

    public static int getKeyLengthInBits(byte[] keyBytes) {
        return keyBytes.length * BIT_MULTIPLIER;
    }

    // --- Validation Operations ---

    public static boolean isValidKeyLength(int keyLengthBits, Integer[] validLengths) {
        return Arrays.stream(validLengths).anyMatch(len -> len == keyLengthBits);
    }

    public static boolean isValidOption(String value, String[] validOptions) {
        return Arrays.asList(validOptions).contains(value);
    }

    // --- Centralized Key Validation & Parsing ---

    public static Result<SecretKeySpec, Error> validateAndParseKey(
            String algorithm,
            String keyBase64,
            Integer[] validKeyLengths) {

        return decodeBase64(keyBase64, "key-base-64")
                .flatMap(keyBytes -> {
                    var keyLengthInBits = getKeyLengthInBits(keyBytes);

                    if (!isValidKeyLength(keyLengthInBits, validKeyLengths)) {
                        return Result.failure(
                                ErrorType.ITS_INVALID_OPTION_PARAMETER,
                                new Field("key-base-64",
                                        "Invalid key length: " + keyLengthInBits + " bits. Valid: " + Arrays.toString(validKeyLengths))
                        );
                    }

                    return Result.success(createKeySpec(keyBytes, algorithm));
                });
    }
}
