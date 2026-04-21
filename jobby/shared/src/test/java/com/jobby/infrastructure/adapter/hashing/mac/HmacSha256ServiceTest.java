package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.MacConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HmacSha256Service - Unit Tests")
class HmacSha256ServiceTest {

    // Clave de 256 bits válida para HmacSHA256
    private static final String VALID_KEY_B64 = Base64.getEncoder().encodeToString(new byte[32]);
    private static final int HMAC_SHA256_OUTPUT_BYTES = 32;

    private HmacSha256Service service;

    @BeforeEach
    void setUp() {
        var config = new MacConfig(VALID_KEY_B64, "HmacSHA256");
        service = new HmacSha256Service(new DefaultMacBuilder(), config);
    }

    // ─── generateMac: entradas vacías / nulas ────────────────────────────────

    @Test
    @DisplayName("generateMac: null retorna success con array vacío")
    void generateMac_nullData_returnsEmptySuccess() {
        var result = service.generateMac(null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("generateMac: cadena en blanco retorna success con array vacío")
    void generateMac_blankData_returnsEmptySuccess() {
        var result = service.generateMac("   ");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isEmpty();
    }

    // ─── generateMac: datos válidos ─────────────────────────────────────────

    @Test
    @DisplayName("generateMac: datos válidos retorna 32 bytes de HMAC-SHA256")
    void generateMac_validData_returns32ByteMac() {
        var result = service.generateMac("hello world");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).hasSize(HMAC_SHA256_OUTPUT_BYTES);
    }

    @Test
    @DisplayName("generateMac: misma entrada produce el mismo HMAC (determinismo)")
    void generateMac_sameData_producesDeterministicMac() {
        var mac1 = service.generateMac("deterministic data");
        var mac2 = service.generateMac("deterministic data");

        assertThat(mac1.isSuccess()).isTrue();
        assertThat(mac2.isSuccess()).isTrue();
        assertThat(mac1.data()).isEqualTo(mac2.data());
    }

    @Test
    @DisplayName("generateMac: entradas distintas producen HMACs distintos")
    void generateMac_differentData_producesDifferentMacs() {
        var mac1 = service.generateMac("data A");
        var mac2 = service.generateMac("data B");

        assertThat(mac1.data()).isNotEqualTo(mac2.data());
    }

    // ─── generateMac: configuración inválida ────────────────────────────────

    @Test
    @DisplayName("generateMac: clave en formato Base64 inválido retorna ITS_SERIALIZATION_ERROR")
    void generateMac_invalidBase64Key_returnsSerializationError() {
        var badConfig = new MacConfig("not::valid@@base64!!!!", "HmacSHA256");
        var badService = new HmacSha256Service(new DefaultMacBuilder(), badConfig);

        var result = badService.generateMac("test data");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_SERIALIZATION_ERROR);
    }

    @Test
    @DisplayName("generateMac: algoritmo inválido en config retorna failure")
    void generateMac_invalidAlgorithmConfig_returnsFailure() {
        var badConfig = new MacConfig(VALID_KEY_B64, "MD5"); // no está en VALID_ALGORITHMS
        var badService = new HmacSha256Service(new DefaultMacBuilder(), badConfig);

        var result = badService.generateMac("test data");

        assertThat(result.isFailure()).isTrue();
    }

    // ─── verifyMac ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("verifyMac: datos correctos y su HMAC retorna true")
    void verifyMac_correctData_returnsTrue() {
        String data = "verify me";
        var macResult = service.generateMac(data);
        assertThat(macResult.isSuccess()).isTrue();

        var result = service.verifyMac(data, macResult.data());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isTrue();
    }

    @Test
    @DisplayName("verifyMac: datos alterados no coinciden con el HMAC original, retorna false")
    void verifyMac_tamperedData_returnsFalse() {
        var macResult = service.generateMac("original data");
        assertThat(macResult.isSuccess()).isTrue();

        var result = service.verifyMac("tampered data", macResult.data());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isFalse();
    }
}
