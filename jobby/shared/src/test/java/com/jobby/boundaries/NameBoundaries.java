package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NameBoundaries {

    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 150;

    public static final List<String> TOO_SHORT_VALUES = List.of("a", "    a     ", "    a", "a    ");

    public static final List<Integer> TOO_LONG_LENGTHS = List.of(151, 200, 250, 500, 1000, 2000, 5000);

    public static final List<Integer> VALID_LENGTHS = List.of(2, 5, 10, 20, 50, 75, 100, 115, 125, 150);

    public static final List<String> VALID_VALUES = List.of(
            "Rodrigo Torres",
            "Ana",
            "Maria Jose Gonzalez Lopez"
    );

    public static String getDescription(String name) {
        if (name == null) return "null";
        if (name.isBlank()) return "blank";
        return "'" + name + "' (" + name.trim().length() + " chars)";
    }

    public static Stream<Arguments> tooShortCases() {
        return TOO_SHORT_VALUES.stream()
                .map(v -> Arguments.of(v, v.trim().length()));
    }

    public static Stream<Arguments> tooLongCases() {
        return TOO_LONG_LENGTHS.stream().map(Arguments::of);
    }

    public static Stream<Arguments> validLengthCases() {
        return VALID_LENGTHS.stream().map(Arguments::of);
    }

    public static Stream<Arguments> validCases() {
        return VALID_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> tooLongAsNames() {
        return TOO_LONG_LENGTHS.stream()
                .map(len -> Arguments.of("a".repeat(len), len));
    }
}
