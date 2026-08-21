package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

public class IdentificationNumberBoundaries {

    public static final List<String> INVALID_FORMAT_VALUES = List.of(
            "abcdefgh",
            "1234567a",
            "a234567890"
    );

    public static final List<String> VALID_VALUES = List.of(
            "12345678",
            "123456789",
            "1234567890"
    );

    public static final List<String> TRIMMABLE_VALUES = List.of(
            "  12345678  ",
            "  1234567890",
            "12345678  "
    );

    public static String getDescription(String number) {
        if (number == null) return "null";
        if (number.isBlank()) return "blank";
        return "'" + number + "'";
    }

    public static Stream<Arguments> tooShort(int minLength) {
        return LengthBoundaries.tooShort(minLength);
    }

    public static Stream<Arguments> tooLong(int maxLength) {
        return LengthBoundaries.tooLong(maxLength);
    }

    public static Stream<Arguments> invalidFormatCases() {
        return INVALID_FORMAT_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> validCases() {
        return VALID_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> trimmableCases() {
        return TRIMMABLE_VALUES.stream().map(Arguments::of);
    }
}
