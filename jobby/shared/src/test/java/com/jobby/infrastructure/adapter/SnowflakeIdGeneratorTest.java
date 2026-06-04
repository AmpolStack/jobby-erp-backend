package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
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

    @Test
    @DisplayName("returns success with positive ID")
    void givenValidConfig_whenNext_returnsSuccessWithPositiveId() {
        var result = generator.next();

        ResultAssertions.assertSuccess(result);
        assertThat(result.data()).isPositive();
    }

    @Test
    @DisplayName("200 consecutive calls produce unique IDs")
    void givenMultipleCalls_whenNext_producesUniqueIds() {
        int calls = 200;

        var ids = IntStream.range(0, calls)
                .mapToLong(i -> {
                    var r = generator.next();
                    ResultAssertions.assertSuccess(r);
                    return r.data();
                })
                .boxed()
                .collect(java.util.stream.Collectors.toSet());

        assertThat(ids).hasSize(calls);
    }

    @Test
    @DisplayName("IDs are strictly increasing")
    void givenSequentialCalls_whenNext_idsAreStrictlyIncreasing() {
        long id1 = generator.next().data();
        long id2 = generator.next().data();

        assertThat(id2).isGreaterThan(id1);
    }

    @Test
    @DisplayName("different worker/datacenter produce different IDs")
    void givenDifferentWorkerAndDatacenter_whenNext_producesDifferentIds() {
        var generator2 = new SnowflakeIdGenerator(new IdConfig(2, 2));

        var idsFromGen1 = new HashSet<Long>();
        var idsFromGen2 = new HashSet<Long>();

        for (int i = 0; i < 50; i++) {
            idsFromGen1.add(generator.next().data());
            idsFromGen2.add(generator2.next().data());
        }

        assertThat(idsFromGen1).doesNotContainAnyElementsOf(idsFromGen2);
    }
}
