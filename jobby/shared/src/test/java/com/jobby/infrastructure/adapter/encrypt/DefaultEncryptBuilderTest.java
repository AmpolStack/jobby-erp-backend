package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultEncryptBuilder - Unit Tests")
class DefaultEncryptBuilderTest {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final byte[] KEY_BYTES_128 = new byte[16];

    private SecretKeySpec validKey;

    @BeforeEach
    void setUp() {
        validKey = new SecretKeySpec(KEY_BYTES_128, "AES");
    }

    @Test
    @DisplayName("valid config returns cipher bytes")
    void givenValidConfig_whenBuild_returnsCipherBytes() {
        var iv = EncryptionCryptography.generateIv(12, 128);
        byte[] plainData = "hello world".getBytes();

        var result = new DefaultEncryptBuilder()
                .setData(plainData)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).isNotEqualTo(plainData);
    }

    @Test
    @DisplayName("roundtrip recovers original data")
    void givenEncryptDecryptRoundtrip_whenBuild_returnsOriginalData() {
        byte[] plainData = "secure message 123".getBytes();
        var iv = EncryptionCryptography.generateIv(12, 128);

        var encryptResult = new DefaultEncryptBuilder()
                .setData(plainData)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();
        ResultAssertions.assertSuccess(encryptResult);

        var decryptResult = new DefaultEncryptBuilder()
                .setData(encryptResult.data())
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.DECRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        ResultAssertions.assertSuccess(decryptResult);
        assertThat(decryptResult.data()).isEqualTo(plainData);
    }

    @Test
    @DisplayName("invalid transformation returns invalid option")
    void givenInvalidTransformation_whenBuild_returnsInvalidOptionParameter() {
        var iv = EncryptionCryptography.generateIv(12, 128);

        var result = new DefaultEncryptBuilder()
                .setData("data".getBytes())
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation("INVALID/UNKNOWN/NoPadding")
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("invalid key size returns operation error")
    void givenInvalidKeySize_whenBuild_returnsOperationError() {
        var invalidKey = new SecretKeySpec(new byte[15], "AES");
        var iv = EncryptionCryptography.generateIv(12, 128);

        var result = new DefaultEncryptBuilder()
                .setData("data".getBytes())
                .setKey(invalidKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_OPERATION_ERROR);
    }

    @Test
    @DisplayName("corrupted ciphertext returns operation error")
    void givenCorruptedCiphertext_whenBuild_returnsOperationError() {
        var iv = EncryptionCryptography.generateIv(12, 128);
        byte[] garbage = new byte[50];

        var result = new DefaultEncryptBuilder()
                .setData(garbage)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.DECRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_OPERATION_ERROR);
    }

    @Test
    @DisplayName("incompatible key type returns operation error")
    void givenIncompatibleKeyType_whenBuild_returnsOperationError() throws NoSuchAlgorithmException {
        var rsaPublicKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();

        var result = new DefaultEncryptBuilder()
                .setData("data".getBytes())
                .setKey(rsaPublicKey)
                .setTransformation(TRANSFORMATION)
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_OPERATION_ERROR);
    }
}
