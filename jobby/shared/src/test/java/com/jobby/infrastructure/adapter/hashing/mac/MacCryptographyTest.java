package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.MacConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MacCryptography - Unit Tests")
class MacCryptographyTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    @DisplayName("throws UnsupportedOperationException when instantiated")
    void givenInstantiated_whenConstructor_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            var ctor = MacCryptography.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("null config returns invalid option")
    void givenNullConfig_whenValidateConfig_returnsInvalidOptionParameter() {
        var result = MacCryptography.validateConfig(null);

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("blank secretKey returns failure")
    void givenBlankSecretKey_whenValidateConfig_returnsFailure() {
        var config = new MacConfig("   ", "HmacSHA256");

        var result = MacCryptography.validateConfig(config);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("invalid algorithm returns failure")
    void givenInvalidAlgorithm_whenValidateConfig_returnsFailure() {
        var config = new MacConfig(VALID_KEY_B64, "MD5");

        var result = MacCryptography.validateConfig(config);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("HmacSHA256 with valid key returns success")
    void givenHmacSha256AndValidKey_whenValidateConfig_returnsSuccess() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA256");

        var result = MacCryptography.validateConfig(config);

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("HmacSHA512 with valid key returns success")
    void givenHmacSha512AndValidKey_whenValidateConfig_returnsSuccess() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA512");

        var result = MacCryptography.validateConfig(config);

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("HmacSHA256 returns true")
    void givenHmacSha256_whenIsValidAlgorithm_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA256")).isTrue();
    }

    @Test
    @DisplayName("HmacSHA1 returns true")
    void givenHmacSha1_whenIsValidAlgorithm_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA1")).isTrue();
    }

    @Test
    @DisplayName("HmacSHA512 returns true")
    void givenHmacSha512_whenIsValidAlgorithm_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA512")).isTrue();
    }

    @Test
    @DisplayName("MD5 returns false")
    void givenMd5_whenIsValidAlgorithm_returnsFalse() {
        assertThat(MacCryptography.isValidAlgorithm("MD5")).isFalse();
    }

    @Test
    @DisplayName("empty string returns false")
    void givenEmptyString_whenIsValidAlgorithm_returnsFalse() {
        assertThat(MacCryptography.isValidAlgorithm("")).isFalse();
    }
}
