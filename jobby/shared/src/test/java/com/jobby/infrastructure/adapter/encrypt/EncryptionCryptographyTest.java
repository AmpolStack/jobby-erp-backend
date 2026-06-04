package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.EncryptConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EncryptionCryptography - Unit Tests")
class EncryptionCryptographyTest {

    @Test
    @DisplayName("throws UnsupportedOperationException when instantiated")
    void givenInstantiated_whenConstructor_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            var ctor = EncryptionCryptography.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("AES 128 generates 16-byte key")
    void givenAes128_whenGenerateKey_returns16ByteKey() throws NoSuchAlgorithmException {
        var key = EncryptionCryptography.generateKey("AES", 128);

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    @DisplayName("AES 256 generates 32-byte key")
    void givenAes256_whenGenerateKey_returns32ByteKey() throws NoSuchAlgorithmException {
        var key = EncryptionCryptography.generateKey("AES", 256);

        assertThat(key).isNotNull();
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    @DisplayName("returns GCMParameterSpec with correct IV and tLen")
    void givenSize12AndTLen128_whenGenerateIv_returnsCorrectSpec() {
        var iv = EncryptionCryptography.generateIv(12, 128);

        assertThat(iv).isNotNull();
        assertThat(iv.getIV()).hasSize(12);
        assertThat(iv.getTLen()).isEqualTo(128);
    }

    @Test
    @DisplayName("consecutive calls produce different IVs")
    void givenCalledTwice_whenGenerateIv_returnsDifferentIvs() {
        var iv1 = EncryptionCryptography.generateIv(12, 128);
        var iv2 = EncryptionCryptography.generateIv(12, 128);

        assertThat(iv1.getIV()).isNotEqualTo(iv2.getIV());
    }

    @Test
    @DisplayName("null config returns invalid option")
    void givenNullConfig_whenValidateConfig_returnsInvalidOptionParameter() {
        var result = EncryptionCryptography.validateConfig(null);

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("blank secretKey returns failure")
    void givenBlankSecretKey_whenValidateConfig_returnsFailure() {
        var config = new EncryptConfig("   ", new EncryptConfig.Iv(12, 128));

        var result = EncryptionCryptography.validateConfig(config);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("invalid tLen returns failure")
    void givenInvalidTLen_whenValidateConfig_returnsFailure() {
        var config = new EncryptConfig("validKey", new EncryptConfig.Iv(12, 64));

        var result = EncryptionCryptography.validateConfig(config);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("valid config returns success")
    void givenValidConfig_whenValidateConfig_returnsSuccess() {
        var config = new EncryptConfig("myValidSecretKey", new EncryptConfig.Iv(12, 128));

        var result = EncryptionCryptography.validateConfig(config);

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("128 is valid")
    void given128_whenIsValidTLen_returnsTrue() {
        assertThat(EncryptionCryptography.isValidTLen(128)).isTrue();
    }

    @Test
    @DisplayName("98 is valid (lower boundary)")
    void given98_whenIsValidTLen_returnsTrue() {
        assertThat(EncryptionCryptography.isValidTLen(98)).isTrue();
    }

    @Test
    @DisplayName("64 is invalid")
    void given64_whenIsValidTLen_returnsFalse() {
        assertThat(EncryptionCryptography.isValidTLen(64)).isFalse();
    }
}
