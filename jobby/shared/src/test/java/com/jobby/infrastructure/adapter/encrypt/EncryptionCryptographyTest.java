package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.EncryptConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EncryptionCryptography - Unit Tests")
class EncryptionCryptographyTest {

    // ─── Utilidad: no instanciable ───────────────────────────────────────────

    @Test
    @DisplayName("constructor: lanza UnsupportedOperationException al intentar instanciar")
    void constructor_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            var ctor = EncryptionCryptography.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    // ─── generateKey ────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateKey: AES 128 bits genera una clave de 16 bytes")
    void generateKey_aes128_generates16ByteKey() throws NoSuchAlgorithmException {
        var key = EncryptionCryptography.generateKey("AES", 128);

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    @DisplayName("generateKey: AES 256 bits genera una clave de 32 bytes")
    void generateKey_aes256_generates32ByteKey() throws NoSuchAlgorithmException {
        var key = EncryptionCryptography.generateKey("AES", 256);

        assertThat(key).isNotNull();
        assertThat(key.getEncoded()).hasSize(32);
    }

    // ─── generateIv ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateIv: genera GCMParameterSpec con el tamaño de IV y tLen correctos")
    void generateIv_withSize12AndTLen128_returnsCorrectSpec() {
        var iv = EncryptionCryptography.generateIv(12, 128);

        assertThat(iv).isNotNull();
        assertThat(iv.getIV()).hasSize(12);
        assertThat(iv.getTLen()).isEqualTo(128);
    }

    @Test
    @DisplayName("generateIv: llamadas consecutivas producen IVs distintos (aleatoriedad)")
    void generateIv_calledTwice_producesDifferentIvs() {
        var iv1 = EncryptionCryptography.generateIv(12, 128);
        var iv2 = EncryptionCryptography.generateIv(12, 128);

        assertThat(iv1.getIV()).isNotEqualTo(iv2.getIV());
    }

    // ─── validateConfig ──────────────────────────────────────────────────────

    @Test
    @DisplayName("validateConfig: config nula retorna INVALID_OPTION_PARAMETER")
    void validateConfig_nullConfig_returnsFailure() {
        var result = EncryptionCryptography.validateConfig(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("validateConfig: secretKey en blanco retorna failure")
    void validateConfig_blankSecretKey_returnsFailure() {
        var config = new EncryptConfig("   ", new EncryptConfig.Iv(12, 128));

        var result = EncryptionCryptography.validateConfig(config);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("validateConfig: tLen inválido (64) retorna failure")
    void validateConfig_invalidTLen_returnsFailure() {
        var config = new EncryptConfig("validKey", new EncryptConfig.Iv(12, 64));

        var result = EncryptionCryptography.validateConfig(config);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("validateConfig: configuración completamente válida retorna success")
    void validateConfig_validConfig_returnsSuccess() {
        var config = new EncryptConfig("myValidSecretKey", new EncryptConfig.Iv(12, 128));

        var result = EncryptionCryptography.validateConfig(config);

        assertThat(result.isSuccess()).isTrue();
    }

    // ─── isValidTLen ────────────────────────────────────────────────────────

    @Test
    @DisplayName("isValidTLen: 128 es un tLen válido")
    void isValidTLen_128_returnsTrue() {
        assertThat(EncryptionCryptography.isValidTLen(128)).isTrue();
    }

    @Test
    @DisplayName("isValidTLen: 98 es un tLen válido (valor límite inferior)")
    void isValidTLen_98_returnsTrue() {
        assertThat(EncryptionCryptography.isValidTLen(98)).isTrue();
    }

    @Test
    @DisplayName("isValidTLen: 64 no es un tLen válido")
    void isValidTLen_64_returnsFalse() {
        assertThat(EncryptionCryptography.isValidTLen(64)).isFalse();
    }
}
