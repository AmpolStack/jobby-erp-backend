package com.jobby.infrastructure.security;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.adapter.CryptoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CryptoUtils Tests")
class CryptoUtilsTest {

    @Nested
    @DisplayName("Base64 Operations")
    class Base64OperationsTest {

        @Test
        @DisplayName("Should successfully decode valid Base64 string")
        void shouldDecodeValidBase64() {
            String original = "Hello, World!";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            assertThat(result.isSuccess()).isTrue();
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("Should successfully decode Base64 with special characters")
        void shouldDecodeBase64WithSpecialCharacters() {
            String original = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            assertThat(result.isSuccess()).isTrue();
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("Should successfully decode Base64 with unicode characters")
        void shouldDecodeBase64WithUnicode() {
            String original = "Unicode: \u00E9\u00E0\u00FC\u00F1\u00E7 \u4E16\u754C \uD83D\uDE00";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            assertThat(result.isSuccess()).isTrue();
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("Should successfully decode empty Base64 string")
        void shouldDecodeEmptyBase64() {
            String encoded = Base64.getEncoder().encodeToString(new byte[0]);

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode() == ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("Should fail with invalid Base64 input")
        void shouldFailWithInvalidBase64() {
            var result = CryptoUtils.decodeBase64("not-valid-base64!!!", "test-field");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("test-field");
        }

        @Test
        @DisplayName("Should fail with blank Base64 input")
        void shouldFailWithBlankBase64() {
            var result = CryptoUtils.decodeBase64("   ", "test-field");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("Should fail with null Base64 input")
        void shouldFailWithNullBase64() {
            var result = CryptoUtils.decodeBase64(null, "test-field");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("Should successfully encode byte array to Base64")
        void shouldEncodeBase64() {
            byte[] data = "Hello, World!".getBytes();

            String encoded = CryptoUtils.encodeBase64(data);

            assertThat(encoded).isEqualTo(Base64.getEncoder().encodeToString(data));
        }

        @Test
        @DisplayName("Should successfully encode empty byte array")
        void shouldEncodeEmptyByteArray() {
            String encoded = CryptoUtils.encodeBase64(new byte[0]);

            assertThat(encoded).isEmpty();
        }

        @Test
        @DisplayName("Should successfully encode byte array with binary data")
        void shouldEncodeBinaryData() {
            byte[] data = new byte[]{0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, (byte) 0xFD};

            String encoded = CryptoUtils.encodeBase64(data);

            assertThat(encoded).isNotBlank();
            // Verify it can be decoded back
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertThat(decoded).containsExactly(data);
        }
    }

    @Nested
    @DisplayName("Key Operations")
    class KeyOperationsTest {

        @Test
        @DisplayName("Should successfully create SecretKeySpec")
        void shouldCreateKeySpec() {
            byte[] keyBytes = new byte[32];
            String algorithm = "AES";

            var keySpec = CryptoUtils.createKeySpec(keyBytes, algorithm);

            assertThat(keySpec).isNotNull();
            assertThat(keySpec.getAlgorithm()).isEqualTo(algorithm);
            assertThat(keySpec.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("Should successfully parse valid KeySpec")
        void shouldParseValidKeySpec() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var keySpec = CryptoUtils.parseKeySpec("AES", encodedKey);

            assertThat(keySpec).isNotNull();
            assertThat(keySpec.getAlgorithm()).isEqualTo("AES");
            assertThat(keySpec.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("Should return null when parsing KeySpec with invalid Base64")
        void shouldReturnNullForInvalidBase64KeySpec() {
            var keySpec = CryptoUtils.parseKeySpec("AES", "not-valid-base64!!!");

            assertThat(keySpec).isNull();
        }

        @Test
        @DisplayName("Should successfully get key length in bits")
        void shouldGetKeyLengthInBits() {
            assertThat(CryptoUtils.getKeyLengthInBits(new byte[16])).isEqualTo(128);
            assertThat(CryptoUtils.getKeyLengthInBits(new byte[24])).isEqualTo(192);
            assertThat(CryptoUtils.getKeyLengthInBits(new byte[32])).isEqualTo(256);
            assertThat(CryptoUtils.getKeyLengthInBits(new byte[0])).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Validation Operations")
    class ValidationOperationsTest {

        @Test
        @DisplayName("Should return true for valid key lengths")
        void shouldReturnTrueForValidKeyLengths() {
            Integer[] validLengths = {128, 192, 256};

            assertThat(CryptoUtils.isValidKeyLength(128, validLengths)).isTrue();
            assertThat(CryptoUtils.isValidKeyLength(192, validLengths)).isTrue();
            assertThat(CryptoUtils.isValidKeyLength(256, validLengths)).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid key lengths")
        void shouldReturnFalseForInvalidKeyLengths() {
            Integer[] validLengths = {128, 192, 256};

            assertThat(CryptoUtils.isValidKeyLength(64, validLengths)).isFalse();
            assertThat(CryptoUtils.isValidKeyLength(512, validLengths)).isFalse();
            assertThat(CryptoUtils.isValidKeyLength(100, validLengths)).isFalse();
        }

        @Test
        @DisplayName("Should return true for valid options")
        void shouldReturnTrueForValidOptions() {
            String[] validOptions = {"AES", "DES", "RSA"};

            assertThat(CryptoUtils.isValidOption("AES", validOptions)).isTrue();
            assertThat(CryptoUtils.isValidOption("DES", validOptions)).isTrue();
            assertThat(CryptoUtils.isValidOption("RSA", validOptions)).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid options")
        void shouldReturnFalseForInvalidOptions() {
            String[] validOptions = {"AES", "DES", "RSA"};

            assertThat(CryptoUtils.isValidOption("Blowfish", validOptions)).isFalse();
            assertThat(CryptoUtils.isValidOption("", validOptions)).isFalse();
            assertThat(CryptoUtils.isValidOption(null, validOptions)).isFalse();
        }
    }

    @Nested
    @DisplayName("Validate and Parse Key")
    class ValidateAndParseKeyTest {

        @Test
        @DisplayName("Should successfully validate and parse valid 256-bit key")
        void shouldValidateAndParseValid256BitKey() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data()).isInstanceOf(SecretKeySpec.class);
            assertThat(result.data().getAlgorithm()).isEqualTo("AES");
        }

        @Test
        @DisplayName("Should successfully validate and parse valid 128-bit key")
        void shouldValidateAndParseValid128BitKey() {
            byte[] keyBytes = new byte[16];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data().getEncoded()).hasSize(16);
        }

        @Test
        @DisplayName("Should successfully validate and parse valid 192-bit key")
        void shouldValidateAndParseValid192BitKey() {
            byte[] keyBytes = new byte[24];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data().getEncoded()).hasSize(24);
        }

        @Test
        @DisplayName("Should fail with invalid key length")
        void shouldFailWithInvalidKeyLength() {
            byte[] keyBytes = new byte[20]; // 160 bits - not in valid lengths
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("key-base-64");
            assertThat(result.error().getFields()[0].getReason()).contains("Invalid key length");
        }

        @Test
        @DisplayName("Should fail with invalid Base64 key")
        void shouldFailWithInvalidBase64Key() {
            var result = CryptoUtils.validateAndParseKey("AES", "not-valid-base64!!!", new Integer[]{128, 192, 256});

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
        }

        @Test
        @DisplayName("Should fail with blank key")
        void shouldFailWithBlankKey() {
            var result = CryptoUtils.validateAndParseKey("AES", "   ", new Integer[]{128, 192, 256});

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("Should fail with null key")
        void shouldFailWithNullKey() {
            var result = CryptoUtils.validateAndParseKey("AES", null, new Integer[]{128, 192, 256});

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }
    }
}
