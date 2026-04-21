package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.infrastructure.configurations.EncryptConfig;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.security.encrypt.EncryptBuilder;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.infrastructure.adapter.CryptoUtils;
import lombok.AllArgsConstructor;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@AllArgsConstructor
public class AESEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private final EncryptBuilder encryptBuilder;
    private final EncryptConfig config;

    @Override
    public Result<String, Error> encryptAsBase64(String data) {
        return encryptAsBytes(data)
                .map(CryptoUtils::encodeBase64);
    }

    @Override
    public Result<byte[], Error> encryptAsBytes(String data) {
        if (data == null || data.isBlank()) {
            return Result.success(new byte[0]);
        }

        return CryptoUtils.validateAndParseKey(
                        ALGORITHM,
                        config.getSecretKey(),
                        EncryptionCryptography.VALID_KEY_LENGTHS_BITS)
                .flatMap(key -> {
                    var iv = EncryptionCryptography.generateIv(
                            config.getIv().getLength(),
                            config.getIv().getTLen());

                    return encryptBuilder
                            .setData(data.getBytes(StandardCharsets.UTF_8))
                            .setIv(iv)
                            .setKey(key)
                            .setMode(Cipher.ENCRYPT_MODE)
                            .setTransformation(TRANSFORMATION)
                            .build()
                            .map(cipherBytes -> {
                                var buffer = ByteBuffer.allocate(iv.getIV().length + cipherBytes.length);
                                buffer.put(iv.getIV());
                                buffer.put(cipherBytes);
                                return buffer.array();
                            });
                });
    }

    @Override
    public Result<String, Error> decryptFromBase64(String cipherText) {
        return CryptoUtils.decodeBase64(cipherText, "cipher-text")
                .flatMap(this::decryptFromBytes);
    }

    @Override
    public Result<String, Error> decryptFromBytes(byte[] combined) {
        if (combined == null || combined.length == 0) {
            return Result.success(null);
        }

        int ivLength = config.getIv().getLength();

        if (combined.length <= ivLength) {
            return Result.failure(
                    ErrorType.ITS_INVALID_OPTION_PARAMETER,
                    new Field("cipher-data", "Data too short to contain IV")
            );
        }

        return CryptoUtils.validateAndParseKey(
                ALGORITHM,
                config.getSecretKey(),
                EncryptionCryptography.VALID_KEY_LENGTHS_BITS)
                .flatMap(key -> {
                        var rawIv = Arrays.copyOfRange(combined, 0, config.getIv().getLength());
                        var data = Arrays.copyOfRange(combined, config.getIv().getLength(), combined.length);
                        var iv = new GCMParameterSpec(config.getIv().getTLen(), rawIv);

                        return encryptBuilder
                                .setData(data)
                                .setIv(iv)
                                .setKey(key)
                                .setMode(Cipher.DECRYPT_MODE)
                                .setTransformation(TRANSFORMATION)
                                .build()
                                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
                });
    }
}
