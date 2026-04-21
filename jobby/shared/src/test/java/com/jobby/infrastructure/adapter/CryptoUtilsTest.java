package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CryptoUtils - Unit Tests")
class CryptoUtilsTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[16]); // 128-bit
    private static final Integer[] AES_VALID_LENGTHS = {128, 192, 256};

    // ─── decodeBase64 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("decodeBase64: entrada Base64 válida retorna bytes decodificados")
    void decodeBase64_validInput_returnsDecodedBytes() {
        byte[] original = "hello-world".getBytes();
        String encoded = Base64.getEncoder().encodeToString(original);

        var result = CryptoUtils.decodeBase64(encoded, "test-field");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("decodeBase64: cadena en blanco retorna failure")
    void decodeBase64_blankInput_returnsFailure() {
        var result = CryptoUtils.decodeBase64("   ", "test-field");

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("decodeBase64: null retorna failure")
    void decodeBase64_nullInput_returnsFailure() {
        var result = CryptoUtils.decodeBase64(null, "test-field");

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("decodeBase64: Base64 inválido retorna error de serialización")
    void decodeBase64_invalidBase64_returnsSerializationError() {
        var result = CryptoUtils.decodeBase64("not::valid@@base64!!!!", "test-field");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
    }

    // ─── encodeBase64 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("encodeBase64: roundtrip encode→decode preserva los bytes originales")
    void encodeBase64_roundtrip_preservesData() {
        byte[] original = {10, 20, 30, 40, 50};

        String encoded = CryptoUtils.encodeBase64(original);
        var decoded = CryptoUtils.decodeBase64(encoded, "field");

        assertThat(decoded.isSuccess()).isTrue();
        assertThat(decoded.data()).isEqualTo(original);
    }

    // ─── getKeyLengthInBits ─────────────────────────────────────────────────

    @Test
    @DisplayName("getKeyLengthInBits: 16 bytes retorna 128 bits")
    void getKeyLengthInBits_16Bytes_returns128Bits() {
        assertThat(CryptoUtils.getKeyLengthInBits(new byte[16])).isEqualTo(128);
    }

    @Test
    @DisplayName("getKeyLengthInBits: 32 bytes retorna 256 bits")
    void getKeyLengthInBits_32Bytes_returns256Bits() {
        assertThat(CryptoUtils.getKeyLengthInBits(new byte[32])).isEqualTo(256);
    }

    // ─── isValidKeyLength ───────────────────────────────────────────────────

    @Test
    @DisplayName("isValidKeyLength: 128 bits es válido para AES")
    void isValidKeyLength_128Bits_returnsTrue() {
        assertThat(CryptoUtils.isValidKeyLength(128, AES_VALID_LENGTHS)).isTrue();
    }

    @Test
    @DisplayName("isValidKeyLength: 64 bits no es válido para AES")
    void isValidKeyLength_64Bits_returnsFalse() {
        assertThat(CryptoUtils.isValidKeyLength(64, AES_VALID_LENGTHS)).isFalse();
    }

    // ─── isValidOption ──────────────────────────────────────────────────────

    @Test
    @DisplayName("isValidOption: opción presente en el arreglo retorna true")
    void isValidOption_presentOption_returnsTrue() {
        assertThat(CryptoUtils.isValidOption("HmacSHA256", new String[]{"HmacSHA1", "HmacSHA256"})).isTrue();
    }

    @Test
    @DisplayName("isValidOption: opción ausente en el arreglo retorna false")
    void isValidOption_absentOption_returnsFalse() {
        assertThat(CryptoUtils.isValidOption("MD5", new String[]{"HmacSHA1", "HmacSHA256"})).isFalse();
    }

    // ─── validateAndParseKey ────────────────────────────────────────────────

    @Test
    @DisplayName("validateAndParseKey: clave AES 128-bit válida retorna SecretKeySpec")
    void validateAndParseKey_valid128BitKey_returnsSecretKeySpec() {
        var result = CryptoUtils.validateAndParseKey("AES", VALID_KEY_B64, AES_VALID_LENGTHS);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isInstanceOf(SecretKeySpec.class);
        assertThat(result.data().getAlgorithm()).isEqualTo("AES");
    }

    @Test
    @DisplayName("validateAndParseKey: Base64 inválido retorna error de serialización")
    void validateAndParseKey_invalidBase64Key_returnsSerializationError() {
        var result = CryptoUtils.validateAndParseKey("AES", "not::valid@@base64", AES_VALID_LENGTHS);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("validateAndParseKey: clave de 64 bits no válida para AES retorna INVALID_OPTION_PARAMETER")
    void validateAndParseKey_invalidKeyLength_returnsInvalidOptionParameter() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[8]); // 64 bits

        var result = CryptoUtils.validateAndParseKey("AES", shortKey, AES_VALID_LENGTHS);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }
}
