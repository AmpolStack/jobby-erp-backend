package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultMacBuilder - Unit Tests")
class DefaultMacBuilderTest {

    private static final String ALGORITHM = "HmacSHA256";
    private static final int HMAC_SHA256_OUTPUT_BYTES = 32;

    private SecretKeySpec validKey;

    @BeforeEach
    void setUp() {
        validKey = new SecretKeySpec(new byte[32], ALGORITHM);
    }

    @Test
    @DisplayName("valid config produces 32-byte MAC")
    void givenValidConfig_whenBuild_returnsMacBytes() {
        var result = new DefaultMacBuilder()
                .setData("test message".getBytes())
                .setKey(validKey)
                .setAlgorithm(ALGORITHM)
                .build();

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).hasSize(HMAC_SHA256_OUTPUT_BYTES);
    }

    @Test
    @DisplayName("same input produces deterministic MAC")
    void givenSameInputAndKey_whenBuild_returnsDeterministicMac() {
        byte[] data = "deterministic input".getBytes();

        var result1 = new DefaultMacBuilder().setData(data).setKey(validKey).setAlgorithm(ALGORITHM).build();
        var result2 = new DefaultMacBuilder().setData(data).setKey(validKey).setAlgorithm(ALGORITHM).build();

        ResultAssertions.assertSuccess(result1);
        assertThat(result1.data()).isEqualTo(result2.data());
    }

    @Test
    @DisplayName("different data produces different MACs")
    void givenDifferentData_whenBuild_returnsDifferentMacs() {
        var mac1 = new DefaultMacBuilder().setData("data A".getBytes()).setKey(validKey).setAlgorithm(ALGORITHM).build();
        var mac2 = new DefaultMacBuilder().setData("data B".getBytes()).setKey(validKey).setAlgorithm(ALGORITHM).build();

        ResultAssertions.assertSuccess(mac1);
        ResultAssertions.assertSuccess(mac2);
        assertThat(mac1.data()).isNotEqualTo(mac2.data());
    }

    @Test
    @DisplayName("invalid algorithm returns invalid option")
    void givenInvalidAlgorithm_whenBuild_returnsInvalidOptionParameter() {
        var result = new DefaultMacBuilder()
                .setData("data".getBytes())
                .setKey(validKey)
                .setAlgorithm("INVALID_HMAC_ALGO")
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("incompatible key type returns operation error")
    void givenIncompatibleKeyType_whenBuild_returnsOperationError() throws NoSuchAlgorithmException {
        var rsaPublicKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();

        var result = new DefaultMacBuilder()
                .setData("data".getBytes())
                .setKey(rsaPublicKey)
                .setAlgorithm(ALGORITHM)
                .build();

        ResultAssertions.assertFailure(result, ErrorType.ITS_OPERATION_ERROR);
    }
}
