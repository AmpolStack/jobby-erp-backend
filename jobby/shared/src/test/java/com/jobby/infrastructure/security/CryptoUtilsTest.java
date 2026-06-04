package com.jobby.infrastructure.security;

import com.jobby.ResultAssertions;
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
        @DisplayName("valid Base64 decodes successfully")
        void givenValidBase64_whenDecodeBase64_returnsDecodedData() {
            String original = "Hello, World!";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            ResultAssertions.assertSuccess(result);
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("special characters decode successfully")
        void givenSpecialChars_whenDecodeBase64_returnsDecodedData() {
            String original = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            ResultAssertions.assertSuccess(result);
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("unicode characters decode successfully")
        void givenUnicode_whenDecodeBase64_returnsDecodedData() {
            String original = "Unicode: \u00E9\u00E0\u00FC\u00F1\u00E7 \u4E16\u754C \uD83D\uDE00";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes());

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            ResultAssertions.assertSuccess(result);
            assertThat(new String(result.data())).isEqualTo(original);
        }

        @Test
        @DisplayName("empty Base64 returns blank error")
        void givenEmptyBase64_whenDecodeBase64_returnsBlankError() {
            String encoded = Base64.getEncoder().encodeToString(new byte[0]);

            var result = CryptoUtils.decodeBase64(encoded, "test-field");

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("invalid Base64 returns serialization error")
        void givenInvalidBase64_whenDecodeBase64_returnsSerializationError() {
            var result = CryptoUtils.decodeBase64("not-valid-base64!!!", "test-field");

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("test-field");
        }

        @Test
        @DisplayName("blank input returns blank error")
        void givenBlank_whenDecodeBase64_returnsBlankError() {
            var result = CryptoUtils.decodeBase64("   ", "test-field");

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("null input returns null error")
        void givenNull_whenDecodeBase64_returnsNullError() {
            var result = CryptoUtils.decodeBase64(null, "test-field");

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("encodes byte array to Base64")
        void givenByteArray_whenEncodeBase64_returnsEncodedString() {
            byte[] data = "Hello, World!".getBytes();

            String encoded = CryptoUtils.encodeBase64(data);

            assertThat(encoded).isEqualTo(Base64.getEncoder().encodeToString(data));
        }

        @Test
        @DisplayName("encodes empty byte array to empty string")
        void givenEmptyArray_whenEncodeBase64_returnsEmptyString() {
            String encoded = CryptoUtils.encodeBase64(new byte[0]);

            assertThat(encoded).isEmpty();
        }

        @Test
        @DisplayName("encodes binary data and decodes back")
        void givenBinaryData_whenEncodeBase64_roundtripsSuccessfully() {
            byte[] data = new byte[]{0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, (byte) 0xFD};

            String encoded = CryptoUtils.encodeBase64(data);

            assertThat(encoded).isNotBlank();
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertThat(decoded).containsExactly(data);
        }
    }

    @Nested
    @DisplayName("Key Operations")
    class KeyOperationsTest {

        @Test
        @DisplayName("creates SecretKeySpec")
        void givenKeyBytesAndAlgorithm_whenCreateKeySpec_returnsSecretKeySpec() {
            byte[] keyBytes = new byte[32];
            String algorithm = "AES";

            var keySpec = CryptoUtils.createKeySpec(keyBytes, algorithm);

            assertThat(keySpec).isNotNull();
            assertThat(keySpec.getAlgorithm()).isEqualTo(algorithm);
            assertThat(keySpec.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("parses valid key spec")
        void givenValidEncodedKey_whenParseKeySpec_returnsSecretKeySpec() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var keySpec = CryptoUtils.parseKeySpec("AES", encodedKey);

            assertThat(keySpec).isNotNull();
            assertThat(keySpec.getAlgorithm()).isEqualTo("AES");
            assertThat(keySpec.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("invalid Base64 returns null key spec")
        void givenInvalidBase64_whenParseKeySpec_returnsNull() {
            var keySpec = CryptoUtils.parseKeySpec("AES", "not-valid-base64!!!");

            assertThat(keySpec).isNull();
        }

        @Test
        @DisplayName("returns key length in bits")
        void givenKeyBytes_whenGetKeyLengthInBits_returnsLength() {
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
        @DisplayName("valid key lengths return true")
        void givenValidLength_whenIsValidKeyLength_returnsTrue() {
            Integer[] validLengths = {128, 192, 256};

            assertThat(CryptoUtils.isValidKeyLength(128, validLengths)).isTrue();
            assertThat(CryptoUtils.isValidKeyLength(192, validLengths)).isTrue();
            assertThat(CryptoUtils.isValidKeyLength(256, validLengths)).isTrue();
        }

        @Test
        @DisplayName("invalid key lengths return false")
        void givenInvalidLength_whenIsValidKeyLength_returnsFalse() {
            Integer[] validLengths = {128, 192, 256};

            assertThat(CryptoUtils.isValidKeyLength(64, validLengths)).isFalse();
            assertThat(CryptoUtils.isValidKeyLength(512, validLengths)).isFalse();
            assertThat(CryptoUtils.isValidKeyLength(100, validLengths)).isFalse();
        }

        @Test
        @DisplayName("valid options return true")
        void givenValidOption_whenIsValidOption_returnsTrue() {
            String[] validOptions = {"AES", "DES", "RSA"};

            assertThat(CryptoUtils.isValidOption("AES", validOptions)).isTrue();
            assertThat(CryptoUtils.isValidOption("DES", validOptions)).isTrue();
            assertThat(CryptoUtils.isValidOption("RSA", validOptions)).isTrue();
        }

        @Test
        @DisplayName("invalid options return false")
        void givenInvalidOption_whenIsValidOption_returnsFalse() {
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
        @DisplayName("valid 256-bit key parses successfully")
        void givenValid256BitKey_whenValidateAndParseKey_returnsKeySpec() {
            byte[] keyBytes = new byte[32];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isInstanceOf(SecretKeySpec.class);
            assertThat(result.data().getAlgorithm()).isEqualTo("AES");
        }

        @Test
        @DisplayName("valid 128-bit key parses successfully")
        void givenValid128BitKey_whenValidateAndParseKey_returnsKeySpec() {
            byte[] keyBytes = new byte[16];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            ResultAssertions.assertSuccess(result);
            assertThat(result.data().getEncoded()).hasSize(16);
        }

        @Test
        @DisplayName("valid 192-bit key parses successfully")
        void givenValid192BitKey_whenValidateAndParseKey_returnsKeySpec() {
            byte[] keyBytes = new byte[24];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            ResultAssertions.assertSuccess(result);
            assertThat(result.data().getEncoded()).hasSize(24);
        }

        @Test
        @DisplayName("invalid key length returns error")
        void givenInvalidKeyLength_whenValidateAndParseKey_returnsInvalidOptionError() {
            byte[] keyBytes = new byte[20];
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

            var result = CryptoUtils.validateAndParseKey("AES", encodedKey, new Integer[]{128, 192, 256});

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("key-base-64");
            assertThat(result.error().getFields()[0].getReason()).contains("Invalid key length");
        }

        @Test
        @DisplayName("invalid Base64 key returns serialization error")
        void givenInvalidBase64Key_whenValidateAndParseKey_returnsSerializationError() {
            var result = CryptoUtils.validateAndParseKey("AES", "not-valid-base64!!!", new Integer[]{128, 192, 256});

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
        }

        @Test
        @DisplayName("blank key returns blank error")
        void givenBlankKey_whenValidateAndParseKey_returnsBlankError() {
            var result = CryptoUtils.validateAndParseKey("AES", "   ", new Integer[]{128, 192, 256});

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("null key returns null error")
        void givenNullKey_whenValidateAndParseKey_returnsNullError() {
            var result = CryptoUtils.validateAndParseKey("AES", null, new Integer[]{128, 192, 256});

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }
    }
}
