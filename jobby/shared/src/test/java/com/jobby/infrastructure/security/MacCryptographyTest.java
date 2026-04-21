package com.jobby.infrastructure.security;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.adapter.hashing.mac.MacCryptography;
import com.jobby.infrastructure.configurations.MacConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MacCryptography Tests")
class MacCryptographyTest {

    @Nested
    @DisplayName("Config Validation")
    class ValidateConfigTest {

        @Test
        @DisplayName("Should successfully validate valid config with HmacSHA256")
        void shouldValidateValidConfigWithHmacSHA256() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should successfully validate valid config with HmacSHA512")
        void shouldValidateValidConfigWithHmacSHA512() {
            byte[] keyBytes = new byte[64];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA512");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should successfully validate valid config with HmacSHA1")
        void shouldValidateValidConfigWithHmacSHA1() {
            byte[] keyBytes = new byte[20];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA1");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with null config")
        void shouldFailWithNullConfig() {
            var result = MacCryptography.validateConfig(null);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("mac-config");
        }

        @Test
        @DisplayName("Should fail validation with blank key")
        void shouldFailWithBlankKey() {
            MacConfig config = new MacConfig();
            config.setSecretKey("   ");
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("Should fail validation with null key")
        void shouldFailWithNullKey() {
            MacConfig config = new MacConfig();
            config.setSecretKey(null);
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("Should fail validation with invalid algorithm")
        void shouldFailWithInvalidAlgorithm() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("InvalidAlgorithm");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("algorithm");
        }

        @Test
        @DisplayName("Should fail validation with blank algorithm")
        void shouldFailWithBlankAlgorithm() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("   ");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with MD5 algorithm (not supported)")
        void shouldFailWithMD5Algorithm() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacMD5");

            var result = MacCryptography.validateConfig(config);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }
    }

    @Nested
    @DisplayName("Algorithm Validation")
    class IsValidAlgorithmTest {

        @Test
        @DisplayName("Should return true for valid algorithms: HmacSHA1, HmacSHA256, HmacSHA512")
        void shouldReturnTrueForValidAlgorithms() {
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA1")).isTrue();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA256")).isTrue();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA512")).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid algorithms")
        void shouldReturnFalseForInvalidAlgorithms() {
            assertThat(MacCryptography.isValidAlgorithm("HmacMD5")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("SHA256")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("AES")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm(null)).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA384")).isFalse();
        }
    }
}
