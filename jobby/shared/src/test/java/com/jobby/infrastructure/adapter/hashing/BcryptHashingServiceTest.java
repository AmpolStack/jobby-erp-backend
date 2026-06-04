package com.jobby.infrastructure.adapter.hashing;

import com.jobby.ResultAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BcryptHashingService - Unit Tests")
class BcryptHashingServiceTest {

    private BcryptHashingService service;

    @BeforeEach
    void setUp() {
        service = new BcryptHashingService();
    }

    @ParameterizedTest(name = "When input is {1}")
    @DisplayName("hash: null or blank returns failure")
    @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
    void givenNullOrBlankInput_whenHash_returnsFailure(String input, String nullityType) {
        var result = service.hash(input);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("input exceeding 72 bytes returns failure")
    void givenInputExceeds72Bytes_whenHash_returnsFailure() {
        String longInput = "a".repeat(100);

        var result = service.hash(longInput);

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("valid input returns Bcrypt hash with $2a$ prefix")
    void givenValidInput_whenHash_returnsBcryptHashWithPrefix() {
        var result = service.hash("securePassword123");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).startsWith("$2a$");
    }

    @Test
    @DisplayName("same input twice produces different hashes")
    void givenSameInputTwice_whenHash_returnsDifferentHashes() {
        var hash1 = service.hash("password");
        var hash2 = service.hash("password");

        ResultAssertions.assertSuccess(hash1);
        ResultAssertions.assertSuccess(hash2);
        assertThat(hash1.data()).isNotEqualTo(hash2.data());
    }

    @Test
    @DisplayName("matching plain returns true")
    void givenMatchingPlain_whenMatches_returnsTrue() {
        var hashResult = service.hash("correctPassword");
        ResultAssertions.assertSuccess(hashResult);

        var result = service.matches("correctPassword", hashResult.data());

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isTrue();
    }

    @Test
    @DisplayName("non-matching plain returns false")
    void givenNonMatchingPlain_whenMatches_returnsFalse() {
        var hashResult = service.hash("correctPassword");
        ResultAssertions.assertSuccess(hashResult);

        var result = service.matches("wrongPassword", hashResult.data());

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isFalse();
    }

    @Test
    @DisplayName("null plain returns failure")
    void givenNullPlain_whenMatches_returnsFailure() {
        var result = service.matches(null, "$2a$10$someValidHashHere");

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("blank hash returns failure")
    void givenBlankHash_whenMatches_returnsFailure() {
        var result = service.matches("password", "   ");

        ResultAssertions.assertFailure(result);
    }
}
