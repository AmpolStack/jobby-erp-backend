package com.jobby.infrastructure.adapter;

import com.jobby.infrastructure.configurations.IdConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SnowflakeIdGenerator - Unit Tests")
class SnowflakeIdGeneratorTest {

    private SnowflakeIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SnowflakeIdGenerator(new IdConfig(1, 1));
    }

    // ─── next ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("next: retorna success con un ID positivo")
    void next_returnsSuccessWithPositiveId() {
        var result = generator.next();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data()).isPositive();
    }

    @Test
    @DisplayName("next: 200 llamadas consecutivas producen IDs únicos")
    void next_multipleCallsReturn_uniqueIds() {
        int calls = 200;

        var ids = IntStream.range(0, calls)
                .mapToLong(i -> {
                    var r = generator.next();
                    assertThat(r.isSuccess()).isTrue();
                    return r.data();
                })
                .boxed()
                .collect(java.util.stream.Collectors.toSet());

        assertThat(ids).hasSize(calls);
    }

    @Test
    @DisplayName("next: IDs son estrictamente crecientes (orden temporal)")
    void next_idsAreStrictlyIncreasing() {
        long id1 = generator.next().data();
        long id2 = generator.next().data();

        assertThat(id2).isGreaterThan(id1);
    }

    @Test
    @DisplayName("next: generadores con distinto workerId/datacenterId producen IDs distintos")
    void next_differentWorkerAndDatacenter_produceDifferentIds() {
        var generator2 = new SnowflakeIdGenerator(new IdConfig(2, 2));

        // Generar un lote de IDs de cada generador y verificar que no se solapan
        var idsFromGen1 = new HashSet<Long>();
        var idsFromGen2 = new HashSet<Long>();

        for (int i = 0; i < 50; i++) {
            idsFromGen1.add(generator.next().data());
            idsFromGen2.add(generator2.next().data());
        }

        // Los conjuntos no deben tener intersección (diferentes bits de worker)
        assertThat(idsFromGen1).doesNotContainAnyElementsOf(idsFromGen2);
    }
}
