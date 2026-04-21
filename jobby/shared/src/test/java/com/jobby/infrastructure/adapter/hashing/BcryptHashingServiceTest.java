package com.jobby.infrastructure.adapter.hashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BcryptHashingService - Unit Tests")
class BcryptHashingServiceTest {

    private BcryptHashingService service;

    @BeforeEach
    void setUp() {
        service = new BcryptHashingService();
    }

    // ─── hash: entradas inválidas ────────────────────────────────────────────

    @Test
    @DisplayName("hash: null retorna failure")
    void hash_nullInput_returnsFailure() {
        var result = service.hash(null);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("hash: cadena en blanco retorna failure")
    void hash_blankInput_returnsFailure() {
        var result = service.hash("   ");

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("hash: entrada que supera 72 bytes (límite BCrypt) retorna failure")
    void hash_inputExceeding72Bytes_returnsFailure() {
        String longInput = "a".repeat(100); // 100 bytes UTF-8

        var result = service.hash(longInput);

        assertThat(result.isFailure()).isTrue();
    }

    // ─── hash: entrada válida ────────────────────────────────────────────────

    @Test
    @DisplayName("hash: entrada válida retorna hash BCrypt con prefijo $2a$")
    void hash_validInput_returnsBcryptHashWithPrefix() {
        var result = service.hash("securePassword123");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).startsWith("$2a$");
    }

    @Test
    @DisplayName("hash: misma entrada produce hashes distintos (salt aleatorio)")
    void hash_sameInputTwice_producesDifferentHashes() {
        var hash1 = service.hash("password");
        var hash2 = service.hash("password");

        assertThat(hash1.isSuccess()).isTrue();
        assertThat(hash2.isSuccess()).isTrue();
        assertThat(hash1.data()).isNotEqualTo(hash2.data());
    }

    // ─── matches ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("matches: plain correcto y su hash retorna true")
    void matches_correctPlainAndHash_returnsTrue() {
        var hashResult = service.hash("correctPassword");
        assertThat(hashResult.isSuccess()).isTrue();

        var result = service.matches("correctPassword", hashResult.data());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isTrue();
    }

    @Test
    @DisplayName("matches: plain incorrecto retorna false")
    void matches_wrongPlain_returnsFalse() {
        var hashResult = service.hash("correctPassword");
        assertThat(hashResult.isSuccess()).isTrue();

        var result = service.matches("wrongPassword", hashResult.data());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isFalse();
    }

    @Test
    @DisplayName("matches: plain null retorna failure")
    void matches_nullPlain_returnsFailure() {
        var result = service.matches(null, "$2a$10$someValidHashHere");

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    @DisplayName("matches: hash en blanco retorna failure")
    void matches_blankHash_returnsFailure() {
        var result = service.matches("password", "   ");

        assertThat(result.isFailure()).isTrue();
    }
}
