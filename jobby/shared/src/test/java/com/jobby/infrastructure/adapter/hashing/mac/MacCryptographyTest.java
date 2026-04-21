package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.MacConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MacCryptography - Unit Tests")
class MacCryptographyTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[32]); // 256 bits

    // ─── Utilidad: no instanciable ───────────────────────────────────────────

    @Test
    @DisplayName("constructor: lanza UnsupportedOperationException al intentar instanciar")
    void constructor_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            var ctor = MacCryptography.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    // ─── validateConfig ──────────────────────────────────────────────────────

    @Test
    @DisplayName("validateConfig: config nula retorna ITS_INVALID_OPTION_PARAMETER")
    void validateConfig_nullConfig_returnsInvalidOptionParameter() {
        var result = MacCryptography.validateConfig(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("validateConfig: secretKey en blanco retorna failure")
    void validateConfig_blankSecretKey_returnsFailure() {
        var config = new MacConfig("   ", "HmacSHA256");

        var result = MacCryptography.validateConfig(config);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("validateConfig: algoritmo inválido retorna failure")
    void validateConfig_invalidAlgorithm_returnsFailure() {
        var config = new MacConfig(VALID_KEY_B64, "MD5");

        var result = MacCryptography.validateConfig(config);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("validateConfig: HmacSHA256 con key válida retorna success")
    void validateConfig_validHmacSha256Config_returnsSuccess() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA256");

        var result = MacCryptography.validateConfig(config);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("validateConfig: HmacSHA512 con key válida retorna success")
    void validateConfig_validHmacSha512Config_returnsSuccess() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA512");

        var result = MacCryptography.validateConfig(config);

        assertThat(result.isSuccess()).isTrue();
    }

    // ─── isValidAlgorithm ───────────────────────────────────────────────────

    @Test
    @DisplayName("isValidAlgorithm: HmacSHA256 retorna true")
    void isValidAlgorithm_hmacSha256_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA256")).isTrue();
    }

    @Test
    @DisplayName("isValidAlgorithm: HmacSHA1 retorna true")
    void isValidAlgorithm_hmacSha1_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA1")).isTrue();
    }

    @Test
    @DisplayName("isValidAlgorithm: HmacSHA512 retorna true")
    void isValidAlgorithm_hmacSha512_returnsTrue() {
        assertThat(MacCryptography.isValidAlgorithm("HmacSHA512")).isTrue();
    }

    @Test
    @DisplayName("isValidAlgorithm: MD5 retorna false")
    void isValidAlgorithm_md5_returnsFalse() {
        assertThat(MacCryptography.isValidAlgorithm("MD5")).isFalse();
    }

    @Test
    @DisplayName("isValidAlgorithm: cadena vacía retorna false")
    void isValidAlgorithm_emptyString_returnsFalse() {
        assertThat(MacCryptography.isValidAlgorithm("")).isFalse();
    }
}
