package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CryptoUtils - Unit Tests")
class CryptoUtilsTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[16]);
    private static final Integer[] AES_VALID_LENGTHS = {128, 192, 256};

    @ParameterizedTest(name = "When encodedData is {1}")
    @DisplayName("decodeBase64: null or blank returns failure")
    @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
    void givenNullOrBlankInput_whenDecodeBase64_returnsFailure(String encodedData, String nullityType) {
        var result = CryptoUtils.decodeBase64(encodedData, "test-field");

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("valid Base64 returns decoded bytes")
    void givenValidInput_whenDecodeBase64_returnsDecodedBytes() {
        byte[] original = "hello-world".getBytes();
        String encoded = Base64.getEncoder().encodeToString(original);

        var result = CryptoUtils.decodeBase64(encoded, "test-field");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("invalid Base64 returns serialization error")
    void givenInvalidBase64_whenDecodeBase64_returnsSerializationError() {
        var result = CryptoUtils.decodeBase64("not::valid@@base64!!!!", "test-field");

        ResultAssertions.assertFailure(result, ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("roundtrip preserves original bytes")
    void givenEncodedBase64_whenDecodeBase64_returnsOriginalData() {
        byte[] original = {10, 20, 30, 40, 50};

        String encoded = CryptoUtils.encodeBase64(original);
        var decoded = CryptoUtils.decodeBase64(encoded, "field");

        ResultAssertions.assertSuccess(decoded);
        assertThat(decoded.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("16 bytes returns 128 bits")
    void given16Bytes_whenGetKeyLengthInBits_returns128Bits() {
        assertThat(CryptoUtils.getKeyLengthInBits(new byte[16])).isEqualTo(128);
    }

    @Test
    @DisplayName("32 bytes returns 256 bits")
    void given32Bytes_whenGetKeyLengthInBits_returns256Bits() {
        assertThat(CryptoUtils.getKeyLengthInBits(new byte[32])).isEqualTo(256);
    }

    @Test
    @DisplayName("128 bits is valid for AES")
    void given128Bits_whenIsValidKeyLength_returnsTrue() {
        assertThat(CryptoUtils.isValidKeyLength(128, AES_VALID_LENGTHS)).isTrue();
    }

    @Test
    @DisplayName("64 bits is not valid for AES")
    void given64Bits_whenIsValidKeyLength_returnsFalse() {
        assertThat(CryptoUtils.isValidKeyLength(64, AES_VALID_LENGTHS)).isFalse();
    }

    @Test
    @DisplayName("present option returns true")
    void givenPresentOption_whenIsValidOption_returnsTrue() {
        assertThat(CryptoUtils.isValidOption("HmacSHA256", new String[]{"HmacSHA1", "HmacSHA256"})).isTrue();
    }

    @Test
    @DisplayName("absent option returns false")
    void givenAbsentOption_whenIsValidOption_returnsFalse() {
        assertThat(CryptoUtils.isValidOption("MD5", new String[]{"HmacSHA1", "HmacSHA256"})).isFalse();
    }

    @Test
    @DisplayName("valid AES key returns SecretKeySpec")
    void givenValidKey_whenValidateAndParseKey_returnsSecretKeySpec() {
        var result = CryptoUtils.validateAndParseKey("AES", VALID_KEY_B64, AES_VALID_LENGTHS);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isInstanceOf(SecretKeySpec.class);
        assertThat(result.data().getAlgorithm()).isEqualTo("AES");
    }

    @Test
    @DisplayName("invalid Base64 returns serialization error")
    void givenInvalidBase64_whenValidateAndParseKey_returnsSerializationError() {
        var result = CryptoUtils.validateAndParseKey("AES", "not::valid@@base64", AES_VALID_LENGTHS);

        ResultAssertions.assertFailure(result, ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("64-bit key returns invalid option")
    void givenInvalidKeyLength_whenValidateAndParseKey_returnsInvalidOptionParameter() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[8]);

        var result = CryptoUtils.validateAndParseKey("AES", shortKey, AES_VALID_LENGTHS);

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }
}
