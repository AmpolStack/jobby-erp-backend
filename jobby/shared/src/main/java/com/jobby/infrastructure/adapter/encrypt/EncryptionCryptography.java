package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.infrastructure.configurations.EncryptConfig;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.infrastructure.adapter.CryptoUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncryptionCryptography {

    public static final Integer[] VALID_T_LENGTHS_BITS = {98, 112, 120, 128};
    public static final Integer[] VALID_KEY_LENGTHS_BITS = {128, 192, 256};

    private EncryptionCryptography() {
        throw new UnsupportedOperationException("Utility class - cannot instantiate");
    }

    // --- AES Key Generation ---

    public static SecretKey generateKey(String algorithm, int size) throws NoSuchAlgorithmException {
        var keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(size);
        return keyGenerator.generateKey();
    }

    // --- GCM IV Generation ---

    public static GCMParameterSpec generateIv(int ivSize, int tLen) {
        var iv = new byte[ivSize];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(tLen, iv);
    }

    // --- Config Validation ---

    public static Result<Void, Error> validateConfig(EncryptConfig config) {
        if (config == null) {
            return Result.failure(
                    com.jobby.domain.mobility.error.ErrorType.ITS_INVALID_OPTION_PARAMETER,
                    new com.jobby.domain.mobility.error.Field("encrypt-config", "Encryption config cannot be null")
            );
        }

        return ValidationChain.create()
                .validateInternalNotBlank(config.getSecretKey(), "secret-key")
                .build()
                .flatMap(v -> ValidationChain.create()
                        .validateInternalAnyMatch(
                                config.getIv().getTLen(),
                                VALID_T_LENGTHS_BITS,
                                "t-len")
                        .build());
    }

    // --- T-Length Validation ---

    public static boolean isValidTLen(int tLen) {
        return CryptoUtils.isValidKeyLength(tLen, VALID_T_LENGTHS_BITS);
    }
}
