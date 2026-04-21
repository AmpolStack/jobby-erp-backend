package com.jobby.infrastructure.security;

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
        @DisplayName("Should successfully generate AES-128 key")
        void shouldGenerateAES128Key() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 128);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(16); // 128 bits = 16 bytes
        }

        @Test
        @DisplayName("Should successfully generate AES-192 key")
        void shouldGenerateAES192Key() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 192);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(24); // 192 bits = 24 bytes
        }

        @Test
        @DisplayName("Should successfully generate AES-256 key")
        void shouldGenerateAES256Key() throws NoSuchAlgorithmException {
            SecretKey key = EncryptionCryptography.generateKey("AES", 256);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(32); // 256 bits = 32 bytes
        }

        @Test
        @DisplayName("Should generate different keys on each call")
        void shouldGenerateDifferentKeys() throws NoSuchAlgorithmException {
            SecretKey key1 = EncryptionCryptography.generateKey("AES", 256);
            SecretKey key2 = EncryptionCryptography.generateKey("AES", 256);

            assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        }
    }

    @Nested
    @DisplayName("GCM IV Generation")
    class GenerateIvTest {

        @Test
        @DisplayName("Should successfully generate IV with size 12 and tLen 128")
        void shouldGenerateIvWithStandardParams() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(12, 128);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(12);
            assertThat(iv.getTLen()).isEqualTo(128);
        }

        @Test
        @DisplayName("Should successfully generate IV with size 16 and tLen 98")
        void shouldGenerateIvWithCustomParams() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(16, 98);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(16);
            assertThat(iv.getTLen()).isEqualTo(98);
        }

        @Test
        @DisplayName("Should generate different IVs each time")
        void shouldGenerateDifferentIvs() {
            GCMParameterSpec iv1 = EncryptionCryptography.generateIv(12, 128);
            GCMParameterSpec iv2 = EncryptionCryptography.generateIv(12, 128);

            assertThat(iv1.getIV()).isNotEqualTo(iv2.getIV());
        }

        @Test
        @DisplayName("Should successfully generate IV with size 8 and tLen 112")
        void shouldGenerateIvWithSmallSize() {
            GCMParameterSpec iv = EncryptionCryptography.generateIv(8, 112);

            assertThat(iv).isNotNull();
            assertThat(iv.getIV()).hasSize(8);
            assertThat(iv.getTLen()).isEqualTo(112);
        }

        @Test
        @DisplayName("Should successfully generate IV with size 1 and tLen 120")
        void shouldGenerateIvWithMinimalSize() {
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
        @DisplayName("Should successfully validate valid config")
        void shouldValidateValidConfig() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with null config")
        void shouldFailWithNullConfig() {
            var result = EncryptionCryptography.validateConfig(null);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("encrypt-config");
        }

        @Test
        @DisplayName("Should fail validation with blank key")
        void shouldFailWithBlankKey() {
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey("   ");
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("Should fail validation with null key")
        void shouldFailWithNullKey() {
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(null);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(128);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("Should fail validation with invalid tLen")
        void shouldFailWithInvalidTLen() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(64); // Invalid tLen
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }

        @Test
        @DisplayName("Should succeed with tLen 98")
        void shouldSucceedWithTLen98() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(98);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should succeed with tLen 112")
        void shouldSucceedWithTLen112() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(112);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should succeed with tLen 120")
        void shouldSucceedWithTLen120() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            EncryptConfig config = new EncryptConfig();
            config.setSecretKey(encodedKey);
            EncryptConfig.Iv iv = new EncryptConfig.Iv();
            iv.setLength(12);
            iv.setTLen(120);
            config.setIv(iv);

            var result = EncryptionCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("T-Length Validation")
    class IsValidTLenTest {

        @Test
        @DisplayName("Should return true for valid tLen values: 98, 112, 120, 128")
        void shouldReturnTrueForValidTLens() {
            assertThat(EncryptionCryptography.isValidTLen(98)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(112)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(120)).isTrue();
            assertThat(EncryptionCryptography.isValidTLen(128)).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid tLen values")
        void shouldReturnFalseForInvalidTLens() {
            assertThat(EncryptionCryptography.isValidTLen(64)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(100)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(256)).isFalse();
            assertThat(EncryptionCryptography.isValidTLen(0)).isFalse();
        }
    }
}
