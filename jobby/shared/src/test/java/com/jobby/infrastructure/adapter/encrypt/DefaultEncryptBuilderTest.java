package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.domain.mobility.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultEncryptBuilder - Unit Tests")
class DefaultEncryptBuilderTest {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final byte[] KEY_BYTES_128 = new byte[16]; // clave AES 128 bits (ceros)

    private SecretKeySpec validKey;

    @BeforeEach
    void setUp() {
        validKey = new SecretKeySpec(KEY_BYTES_128, "AES");
    }

    // ─── build: camino feliz ─────────────────────────────────────────────────

    @Test
    @DisplayName("build: configuración AES/GCM válida encripta y retorna bytes con éxito")
    void build_validEncryptConfig_returnsCipherBytes() {
        var iv = EncryptionCryptography.generateIv(12, 128);
        byte[] plainData = "hello world".getBytes();

        var result = new DefaultEncryptBuilder()
                .setData(plainData)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).isNotEqualTo(plainData); // ciphertext ≠ plaintext
    }

    @Test
    @DisplayName("build: roundtrip encrypt → decrypt recupera el plaintext original")
    void build_encryptDecryptRoundtrip_recoversOriginalData() {
        byte[] plainData = "secure message 123".getBytes();
        var iv = EncryptionCryptography.generateIv(12, 128);

        var encryptResult = new DefaultEncryptBuilder()
                .setData(plainData)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();
        assertThat(encryptResult.isSuccess()).isTrue();

        var decryptResult = new DefaultEncryptBuilder()
                .setData(encryptResult.data())
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.DECRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        assertThat(decryptResult.isSuccess()).isTrue();
        assertThat(decryptResult.data()).isEqualTo(plainData);
    }

    // ─── build: caminos de error ─────────────────────────────────────────────

    @Test
    @DisplayName("build: transformación inválida retorna INVALID_OPTION_PARAMETER")
    void build_invalidTransformation_returnsInvalidOptionParameter() {
        var iv = EncryptionCryptography.generateIv(12, 128);

        var result = new DefaultEncryptBuilder()
                .setData("data".getBytes())
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation("INVALID/UNKNOWN/NoPadding")
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("build: clave de tamaño inválido para AES retorna ITS_OPERATION_ERROR")
    void build_invalidKeySizeForAES_returnsOperationError() {
        // 15 bytes (120 bits) no es un tamaño válido para AES en modo JCE
        var invalidKey = new SecretKeySpec(new byte[15], "AES");
        var iv = EncryptionCryptography.generateIv(12, 128);

        var result = new DefaultEncryptBuilder()
                .setData("data".getBytes())
                .setKey(invalidKey)
                .setIv(iv)
                .setMode(Cipher.ENCRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_OPERATION_ERROR);
    }

    @Test
    @DisplayName("build: datos de cipher corruptos en modo DECRYPT retorna ITS_OPERATION_ERROR")
    void build_corruptedCiphertextInDecryptMode_returnsOperationError() {
        var iv = EncryptionCryptography.generateIv(12, 128);
        byte[] garbage = new byte[50]; // datos aleatorios, no cifrado válido

        var result = new DefaultEncryptBuilder()
                .setData(garbage)
                .setKey(validKey)
                .setIv(iv)
                .setMode(Cipher.DECRYPT_MODE)
                .setTransformation(TRANSFORMATION)
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_OPERATION_ERROR);
    }
}
