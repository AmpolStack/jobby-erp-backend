package com.jobby.infrastructure.adapter.hashing.mac;

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
    // HmacSHA256 produce exactamente 32 bytes (256 bits)
    private static final int HMAC_SHA256_OUTPUT_BYTES = 32;

    private SecretKeySpec validKey;

    @BeforeEach
    void setUp() {
        validKey = new SecretKeySpec(new byte[32], ALGORITHM);
    }

    // ─── build: camino feliz ─────────────────────────────────────────────────

    @Test
    @DisplayName("build: configuración válida produce 32 bytes de HMAC-SHA256")
    void build_validConfiguration_returnsMacBytes() {
        var result = new DefaultMacBuilder()
                .setData("test message".getBytes())
                .setKey(validKey)
                .setAlgorithm(ALGORITHM)
                .build();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).hasSize(HMAC_SHA256_OUTPUT_BYTES);
    }

    @Test
    @DisplayName("build: misma clave y datos producen HMAC idéntico (determinismo)")
    void build_sameInputAndKey_producesDeterministicMac() {
        byte[] data = "deterministic input".getBytes();

        var result1 = new DefaultMacBuilder().setData(data).setKey(validKey).setAlgorithm(ALGORITHM).build();
        var result2 = new DefaultMacBuilder().setData(data).setKey(validKey).setAlgorithm(ALGORITHM).build();

        assertThat(result1.isSuccess()).isTrue();
        assertThat(result1.data()).isEqualTo(result2.data());
    }

    @Test
    @DisplayName("build: datos distintos producen HMACs distintos")
    void build_differentData_producesDifferentMacs() {
        var mac1 = new DefaultMacBuilder().setData("data A".getBytes()).setKey(validKey).setAlgorithm(ALGORITHM).build();
        var mac2 = new DefaultMacBuilder().setData("data B".getBytes()).setKey(validKey).setAlgorithm(ALGORITHM).build();

        assertThat(mac1.data()).isNotEqualTo(mac2.data());
    }

    // ─── build: caminos de error ─────────────────────────────────────────────

    @Test
    @DisplayName("build: algoritmo inválido retorna ITS_INVALID_OPTION_PARAMETER")
    void build_invalidAlgorithm_returnsInvalidOptionParameter() {
        var result = new DefaultMacBuilder()
                .setData("data".getBytes())
                .setKey(validKey)
                .setAlgorithm("INVALID_HMAC_ALGO")
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
    }

    @Test
    @DisplayName("build: clave RSA pública (tipo incompatible) retorna ITS_OPERATION_ERROR")
    void build_incompatibleRsaPublicKey_returnsOperationError() throws NoSuchAlgorithmException {
        // Una clave pública RSA es incompatible con Mac.init() que espera SecretKey
        var rsaPublicKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();

        var result = new DefaultMacBuilder()
                .setData("data".getBytes())
                .setKey(rsaPublicKey)
                .setAlgorithm(ALGORITHM)
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_OPERATION_ERROR);
    }
}
