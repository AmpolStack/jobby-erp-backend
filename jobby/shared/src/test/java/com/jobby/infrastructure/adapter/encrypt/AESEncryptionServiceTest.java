package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.EncryptConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AESEncryptionService - Unit Tests")
class AESEncryptionServiceTest {

    // Clave AES 128 bits válida codificada en Base64
    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[16]);
    // Clave de 64 bits inválida para AES
    private static final String INVALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[8]);

    private AESEncryptionService service;

    @BeforeEach
    void setUp() {
        var iv = new EncryptConfig.Iv(12, 128);
        var config = new EncryptConfig(VALID_KEY_B64, iv);
        service = new AESEncryptionService(new DefaultEncryptBuilder(), config);
    }

    // ─── encrypt: entradas vacías / nulas ────────────────────────────────────

    @Test
    @DisplayName("encrypt: null retorna success con array vacío")
    void encrypt_nullData_returnsEmptySuccess() {
        var result = service.encrypt(null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("encrypt: cadena en blanco retorna success con array vacío")
    void encrypt_blankData_returnsEmptySuccess() {
        var result = service.encrypt("   ");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isEmpty();
    }

    // ─── encrypt: datos válidos ─────────────────────────────────────────────

    @Test
    @DisplayName("encrypt: datos válidos retorna bytes cifrados no vacíos")
    void encrypt_validData_returnsCipherBytes() {
        var result = service.encrypt("sensitive information");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isNotEmpty();
    }

    @Test
    @DisplayName("encrypt: la misma entrada produce salidas distintas (IV aleatorio)")
    void encrypt_sameInputTwice_producesDifferentCiphertexts() {
        var result1 = service.encrypt("same data");
        var result2 = service.encrypt("same data");

        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();
        assertThat(result1.data()).isNotEqualTo(result2.data()); // IVs distintos
    }

    // ─── encryptAsBase64 ────────────────────────────────────────────────────

    @Test
    @DisplayName("encryptAsBase64: retorna cadena Base64 no vacía y decodificable")
    void encryptAsBase64_validData_returnsDecodableBase64() {
        var result = service.encryptAsBase64("my secret value");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isNotBlank();
        // verificar que es Base64 válido
        assertThat(Base64.getDecoder().decode(result.data())).isNotEmpty();
    }

    // ─── decrypt ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("decrypt: roundtrip encryptAsBase64 → decrypt recupera el texto original")
    void decrypt_afterEncryptAsBase64_returnsOriginalPlaintext() {
        String original = "the quick brown fox";

        var encrypted = service.encryptAsBase64(original);
        assertThat(encrypted.isSuccess()).isTrue();

        var decrypted = service.decrypt(encrypted.data());

        assertThat(decrypted.isSuccess()).isTrue();
        assertThat(decrypted.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("decrypt: Base64 inválido retorna ITS_SERIALIZATION_ERROR")
    void decrypt_invalidBase64_returnsSerializationError() {
        var result = service.decrypt("not::valid@@base64!!!!");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("decrypt: ciphertext más corto que el IV retorna ITS_INVALID_OPTION_PARAMETER")
    void decrypt_ciphertextShorterThanIV_returnsInvalidOptionParameter() {
        // Sólo 4 bytes: menor que el IV de 12 bytes
        String tooShort = Base64.getEncoder().encodeToString(new byte[4]);

        var result = service.decrypt(tooShort);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    // ─── configuración inválida ────────────────────────────────────────────

    @Test
    @DisplayName("encrypt: clave de 64 bits (inválida para AES) retorna ITS_INVALID_OPTION_PARAMETER")
    void encrypt_invalidKeyConfiguration_returnsInvalidOptionParameter() {
        var badConfig = new EncryptConfig(INVALID_KEY_B64, new EncryptConfig.Iv(12, 128));
        var badService = new AESEncryptionService(new DefaultEncryptBuilder(), badConfig);

        var result = badService.encrypt("some data");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }
}
