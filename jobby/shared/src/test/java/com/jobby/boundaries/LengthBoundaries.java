package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LengthBoundaries {

    public static Stream<Arguments> tooShort(int minLength) {
        return IntStream.range(1, minLength)
                .mapToObj(len -> Arguments.of("a".repeat(len), len));
    }

    public static Stream<Arguments> tooLong(int maxLength) {
        return IntStream.of(maxLength + 1, maxLength + 10, maxLength + 50, maxLength + 100, maxLength + 500, maxLength + 1000)
                .mapToObj(len -> Arguments.of("a".repeat(len), len));
    }

    public static Stream<Arguments> atBoundaries(int minLength, int maxLength) {
        int mid = minLength + (maxLength - minLength) / 2;
        return IntStream.of(minLength, mid, maxLength)
                .mapToObj(Arguments::of);
    }

    public static Stream<Arguments> atBoundariesWithPadding(int minLength, int maxLength) {
        int mid = minLength + (maxLength - minLength) / 2;
        return Stream.of(
                Arguments.of("a".repeat(minLength), minLength),
                Arguments.of("a".repeat(mid), mid),
                Arguments.of("a".repeat(maxLength), maxLength)
        );
    }

    public static Stream<Arguments> outsideRange(int minLength, int maxLength) {
        return Stream.concat(
                tooShort(minLength),
                tooLong(maxLength)
        );
    }
}
