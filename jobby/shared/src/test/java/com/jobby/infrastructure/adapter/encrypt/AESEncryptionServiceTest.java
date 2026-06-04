package com.jobby.infrastructure.adapter.encrypt;

import com.jobby.boundaries.NullityBoundaries;
import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.EncryptConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AESEncryptionService - Unit Tests")
class AESEncryptionServiceTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[16]);
    private static final String INVALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[8]);

    private AESEncryptionService service;

    @BeforeEach
    void setUp() {
        var iv = new EncryptConfig.Iv(12, 128);
        var config = new EncryptConfig(VALID_KEY_B64, iv);
        service = new AESEncryptionService(new DefaultEncryptBuilder(), config);
    }

    @ParameterizedTest(name = "When data is {1}")
    @DisplayName("null or blank returns empty success")
    @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
    void givenNullOrBlankData_whenEncryptAsBytes_returnsEmptySuccess(String data, String nullityType) {
        var result = service.encryptAsBytes(data);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("valid data returns cipher bytes")
    void givenValidData_whenEncryptAsBytes_returnsCipherBytes() {
        var result = service.encryptAsBytes("sensitive information");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isNotEmpty();
    }

    @Test
    @DisplayName("same input twice produces different ciphertexts")
    void givenSameDataTwice_whenEncryptAsBytes_returnsDifferentCiphertexts() {
        var result1 = service.encryptAsBytes("same data");
        var result2 = service.encryptAsBytes("same data");

        ResultAssertions.assertSuccess(result1);
        ResultAssertions.assertSuccess(result2);
        assertThat(result1.data()).isNotEqualTo(result2.data());
    }

    @ParameterizedTest(name = "When data is {1}")
    @DisplayName("null or blank returns empty string")
    @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
    void givenNullOrBlankData_whenEncryptAsBase64_returnsEmptyString(String data, String nullityType) {
        var result = service.encryptAsBase64(data);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("valid data returns decodable Base64")
    void givenValidData_whenEncryptAsBase64_returnsDecodableBase64() {
        var result = service.encryptAsBase64("my secret value");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isNotBlank();
        assertThat(Base64.getDecoder().decode(result.data())).isNotEmpty();
    }

    @Test
    @DisplayName("null or empty returns null")
    void givenNullOrEmptyData_whenDecryptFromBytes_returnsNull() {
        var nullResult = service.decryptFromBytes(null);
        var emptyResult = service.decryptFromBytes(new byte[0]);

        ResultAssertions.assertSuccess(nullResult);
        assertThat(nullResult.data()).isNull();
        ResultAssertions.assertSuccess(emptyResult);
        assertThat(emptyResult.data()).isNull();
    }

    @Test
    @DisplayName("roundtrip returns original plaintext")
    void givenEncryptedBytes_whenDecryptFromBytes_returnsOriginalPlaintext() {
        String original = "the quick brown fox";

        var encrypted = service.encryptAsBytes(original);
        ResultAssertions.assertSuccess(encrypted);

        var decrypted = service.decryptFromBytes(encrypted.data());
        ResultAssertions.assertSuccess(decrypted);
        assertThat(decrypted.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("roundtrip returns original plaintext")
    void givenEncryptedBase64_whenDecryptFromBase64_returnsOriginalPlaintext() {
        String original = "the quick brown fox";

        var encrypted = service.encryptAsBase64(original);
        ResultAssertions.assertSuccess(encrypted);

        var decrypted = service.decryptFromBase64(encrypted.data());
        ResultAssertions.assertSuccess(decrypted);
        assertThat(decrypted.data()).isEqualTo(original);
    }

    @Test
    @DisplayName("invalid Base64 returns serialization error")
    void givenInvalidBase64_whenDecryptFromBase64_returnsSerializationError() {
        var result = service.decryptFromBase64("not::valid@@base64!!!!");

        ResultAssertions.assertFailure(result, ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("ciphertext shorter than IV returns invalid option")
    void givenCiphertextShorterThanIV_whenDecryptFromBase64_returnsInvalidOptionParameter() {
        String tooShort = Base64.getEncoder().encodeToString(new byte[4]);

        var result = service.decryptFromBase64(tooShort);

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("ciphertext shorter than IV returns invalid option")
    void givenCiphertextShorterThanIV_whenDecryptFromBytes_returnsInvalidOptionParameter() {
        byte[] tooShort = new byte[4];

        var result = service.decryptFromBytes(tooShort);

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("invalid AES key returns invalid option")
    void givenInvalidKey_whenEncryptAsBytes_returnsInvalidOptionParameter() {
        var badConfig = new EncryptConfig(INVALID_KEY_B64, new EncryptConfig.Iv(12, 128));
        var badService = new AESEncryptionService(new DefaultEncryptBuilder(), badConfig);

        var result = badService.encryptAsBytes("some data");

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }
}
