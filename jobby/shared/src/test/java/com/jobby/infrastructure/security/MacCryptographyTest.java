package com.jobby.infrastructure.security;

import com.jobby.ResultAssertions;
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
        @DisplayName("valid HmacSHA256 config passes")
        void givenHmacSha256Config_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("valid HmacSHA512 config passes")
        void givenHmacSha512Config_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[64];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA512");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("valid HmacSHA1 config passes")
        void givenHmacSha1Config_whenValidateConfig_returnsSuccess() {
            byte[] keyBytes = new byte[20];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacSHA1");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("null config fails")
        void givenNullConfig_whenValidateConfig_returnsInvalidOptionError() {
            var result = MacCryptography.validateConfig(null);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("mac-config");
        }

        @Test
        @DisplayName("blank key fails")
        void givenBlankKey_whenValidateConfig_returnsBlankError() {
            MacConfig config = new MacConfig();
            config.setSecretKey("   ");
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("null key fails")
        void givenNullKey_whenValidateConfig_returnsNullError() {
            MacConfig config = new MacConfig();
            config.setSecretKey(null);
            config.setAlgorithm("HmacSHA256");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("invalid algorithm fails")
        void givenInvalidAlgorithm_whenValidateConfig_returnsInvalidOptionError() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("InvalidAlgorithm");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("algorithm");
        }

        @Test
        @DisplayName("blank algorithm fails")
        void givenBlankAlgorithm_whenValidateConfig_returnsFailure() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("   ");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
        }

        @Test
        @DisplayName("MD5 algorithm fails")
        void givenMd5Algorithm_whenValidateConfig_returnsInvalidOptionError() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            MacConfig config = new MacConfig();
            config.setSecretKey(encodedKey);
            config.setAlgorithm("HmacMD5");

            var result = MacCryptography.validateConfig(config);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }
    }

    @Nested
    @DisplayName("Algorithm Validation")
    class IsValidAlgorithmTest {

        @Test
        @DisplayName("valid algorithms return true")
        void givenValidAlgorithm_whenIsValidAlgorithm_returnsTrue() {
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA1")).isTrue();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA256")).isTrue();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA512")).isTrue();
        }

        @Test
        @DisplayName("invalid algorithms return false")
        void givenInvalidAlgorithm_whenIsValidAlgorithm_returnsFalse() {
            assertThat(MacCryptography.isValidAlgorithm("HmacMD5")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("SHA256")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("AES")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("")).isFalse();
            assertThat(MacCryptography.isValidAlgorithm(null)).isFalse();
            assertThat(MacCryptography.isValidAlgorithm("HmacSHA384")).isFalse();
        }
    }
}
