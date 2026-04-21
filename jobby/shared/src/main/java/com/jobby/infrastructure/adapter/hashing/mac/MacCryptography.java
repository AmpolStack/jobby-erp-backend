package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.infrastructure.adapter.CryptoUtils;
import com.jobby.infrastructure.configurations.MacConfig;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;

public final class MacCryptography {

    public static final Integer[] VALID_KEY_LENGTHS_BITS = {128, 160, 192, 224, 256, 384, 512};
    public static final String[] VALID_ALGORITHMS = {"HmacSHA1", "HmacSHA256", "HmacSHA512"};

    private MacCryptography() {
        throw new UnsupportedOperationException("Utility class - cannot instantiate");
    }

    // --- Config Validation ---

    public static Result<Void, Error> validateConfig(MacConfig config) {
        if (config == null) {
            return Result.failure(
                    com.jobby.domain.mobility.error.ErrorType.ITS_INVALID_OPTION_PARAMETER,
                    new com.jobby.domain.mobility.error.Field("mac-config", "MAC config cannot be null")
            );
        }

        return ValidationChain.create()
                .validateInternalNotBlank(config.getSecretKey(), "secret-key")
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateInternalAnyMatch(
                                config.getAlgorithm(),
                                VALID_ALGORITHMS,
                                "algorithm")
                        .build());
    }

    // --- Algorithm Validation ---

    public static boolean isValidAlgorithm(String algorithm) {
        return CryptoUtils.isValidOption(algorithm, VALID_ALGORITHMS);
    }
}
