package com.jobby.infrastructure.security;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.adapter.encrypt.EncryptionCryptography;
import com.jobby.infrastructure.configurations.EncryptConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EncryptionCryptography Tests")
class EncryptionCryptographyTest {

    @Nested
    @DisplayName("AES Key Generation")
    class GenerateKeyTest {

        @Test
        @DisplayName("generates AES-128 key")
        void givenAes128_whenGenerateKey_returns16ByteKey() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 128);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(16);
        }

        @Test
        @DisplayName("generates AES-192 key")
        void givenAes192_whenGenerateKey_returns24ByteKey() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 192);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(24);
        }

        @Test
        @DisplayName("generates AES-256 key")
        void givenAes256_whenGenerateKey_returns32ByteKey() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 256);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("generates different keys each call")
        void givenMultipleCalls_whenGenerateKey_returnsDifferentKeys() throws NoSuchAlgorithmException {
            SecretKey key1 = EncryptionCryptography.generateKey("AES", 256);
            SecretKey key2 = EncryptionCryptography.generateKey("AES", 256);

            assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        }
    }

    @Nested
    @DisplayName("GCM IV Generation")
    class GenerateIvTest {

        @Test
        @DisplayName("generates IV with standard params")
        void givenStandardParams_whenGenerateIv_returnsCorrectSpec() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(12, 128);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(12);
            assertThat(iv.getTLen()).isEqualTo(128);
        }

        @Test
        @DisplayName("generates IV with custom params")
        void givenCustomParams_whenGenerateIv_returnsCorrectSpec() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(16, 98);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(16);
            assertThat(iv.getTLen()).isEqualTo(98);
        }

        @Test
        @DisplayName("generates different IVs each call")
        void givenMultipleCalls_whenGenerateIv_returnsDifferentIvs() {
            GCMParameterSpec iv1 = EncryptionCryptography.generateIv(12, 128);
            GCMParameterSpec iv2 = EncryptionCryptography.generateIv(12, 128);

            assertThat(iv1.getIV()).isNotEqualTo(iv2.getIV());
        }

        @Test
        @DisplayName("generates IV with size 8")
        void givenSize8_whenGenerateIv_returnsCorrectSpec() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(8, 112);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(8);
            assertThat(iv.getTLen()).isEqualTo(112);
        }

        @Test
        @DisplayName("generates IV with minimal size")
        void givenMinimalSize_whenGenerateIv_returnsCorrectSpec() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(1, 120);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(1);
            assertThat(iv.getTLen()).isEqualTo(120);
        }
    }

    @Nested
    @DisplayName("Config Validation")
    class ValidateConfigTest {

        @Test
        @DisplayName("valid config passes")
        void givenValidConfig_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("null config fails")
        void givenNullConfig_whenValidateConfig_returnsInvalidOptionError() {
            var result = EncryptionCryptography.validateConfig(null);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("encrypt-config");
        }

        @Test
        @DisplayName("blank key fails")
        void givenBlankKey_whenValidateConfig_returnsBlankError() {
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey("   ");
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("null key fails")
        void givenNullKey_whenValidateConfig_returnsNullError() {
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(null);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("invalid tLen fails")
        void givenInvalidTLen_whenValidateConfig_returnsInvalidOptionError() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(64);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }

        @Test
        @DisplayName("tLen 98 passes")
        void givenTLen98_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(98);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("tLen 112 passes")
        void givenTLen112_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(112);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("tLen 120 passes")
        void givenTLen120_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(120);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }
    }

    @Nested
    @DisplayName("T-Length Validation")
    class IsValidTLenTest {

        @Test
        @DisplayName("valid tLens return true")
        void givenValidTLen_whenIsValidTLen_returnsTrue() {
            assertThat(EncryptionCryptography.isValidTLen(98)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(112)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(120)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(128)).isTrue();
        }

        @Test
        @DisplayName("invalid tLens return false")
        void givenInvalidTLen_whenIsValidTLen_returnsFalse() {
            assertThat(EncryptionCryptography.isValidTLen(64)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(100)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(256)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(0)).isFalse();
        }
    }
}
