package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.boundaries.NullityBoundaries;
import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.MacConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HmacSha256Service - Unit Tests")
class HmacSha256ServiceTest {

    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[32]);
    private static final int HMAC_SHA256_OUTPUT_BYTES = 32;

    private HmacSha256Service service;

    @BeforeEach
    void setUp() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA256");
        service = new HmacSha256Service(new DefaultMacBuilder(), config);
    }

    @ParameterizedTest(name = "When data is {1}")
    @DisplayName("null or blank returns empty success")
    @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
    void givenNullOrBlankData_whenGenerateMac_returnsEmptySuccess(String data, String nullityType) {
        var result = service.generateMac(data);

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("valid data returns 32-byte MAC")
    void givenValidData_whenGenerateMac_returns32ByteMac() {
        var result = service.generateMac("hello world");

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).hasSize(HMAC_SHA256_OUTPUT_BYTES);
    }

    @Test
    @DisplayName("same data produces deterministic MAC")
    void givenSameData_whenGenerateMac_returnsDeterministicMac() {
        var mac1 = service.generateMac("deterministic data");
        var mac2 = service.generateMac("deterministic data");

        ResultAssertions.assertSuccess(mac1);
        ResultAssertions.assertSuccess(mac2);
        assertThat(mac1.data()).isEqualTo(mac2.data());
    }

    @Test
    @DisplayName("different data produces different MACs")
    void givenDifferentData_whenGenerateMac_returnsDifferentMacs() {
        var mac1 = service.generateMac("data A");
        var mac2 = service.generateMac("data B");

        ResultAssertions.assertSuccess(mac1);
        ResultAssertions.assertSuccess(mac2);
        assertThat(mac1.data()).isNotEqualTo(mac2.data());
    }

    @Test
    @DisplayName("invalid Base64 key returns serialization error")
    void givenInvalidKeyBase64_whenGenerateMac_returnsSerializationError() {
        var badConfig = new MacConfig("not::valid@@base64!!!!", "HmacSHA256");
        var badService = new HmacSha256Service(new DefaultMacBuilder(), badConfig);

        var result = badService.generateMac("test data");

        ResultAssertions.assertFailure(result, ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("invalid algorithm returns failure")
    void givenInvalidAlgorithm_whenGenerateMac_returnsFailure() {
        var badConfig = new MacConfig(VALID_KEY_B64, "MD5");
        var badService = new HmacSha256Service(new DefaultMacBuilder(), badConfig);

        var result = badService.generateMac("test data");

        ResultAssertions.assertFailure(result);
    }

    @Test
    @DisplayName("matching data returns true")
    void givenMatchingData_whenVerifyMac_returnsTrue() {
        String data = "verify me";
        var macResult = service.generateMac(data);
        ResultAssertions.assertSuccess(macResult);

        var result = service.verifyMac(data, macResult.data());

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isTrue();
    }

    @Test
    @DisplayName("tampered data returns false")
    void givenTamperedData_whenVerifyMac_returnsFalse() {
        var macResult = service.generateMac("original data");
        ResultAssertions.assertSuccess(macResult);

        var result = service.verifyMac("tampered data", macResult.data());

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isFalse();
    }
}
