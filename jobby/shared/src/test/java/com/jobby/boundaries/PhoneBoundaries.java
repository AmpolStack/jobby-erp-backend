package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

public class PhoneBoundaries {

    public static final List<String> VALID_VALUES = List.of(
            "3001234567",
            "3101234567",
            "3201234567",
            "3991234567"
    );

    public static final List<String> INVALID_FORMAT_VALUES = List.of(
            "1001234567",
            "300123456",
            "30012345678",
            "3001234abc",
            "+573001234567"
    );

    public static final List<String> ALL_INVALID = Stream.concat(
            NullityBoundaries.BLANK_VALUES.stream(),
            INVALID_FORMAT_VALUES.stream()
    ).toList();

    public static final List<Arguments> E164_CASES = List.of(
            Arguments.of("3001234567", "+57 3001234567"),
            Arguments.of("3101234567", "+57 3101234567"),
            Arguments.of("3201234567", "+57 3201234567")
    );

    public static final List<Arguments> DISPLAY_CASES = List.of(
            Arguments.of("3001234567", "(+57) 3001234567"),
            Arguments.of("3101234567", "(+57) 3101234567"),
            Arguments.of("3201234567", "(+57) 3201234567")
    );

    public static String getDescription(String phone) {
        if (phone == null) return "null";
        if (phone.isBlank()) return "blank";
        return phone;
    }

    public static Stream<Arguments> validCases() {
        return VALID_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidFormatCases() {
        return INVALID_FORMAT_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidFormatCasesWithName() {
        return INVALID_FORMAT_VALUES.stream()
                .map(v -> Arguments.of(v, getDescription(v)));
    }

    public static Stream<Arguments> allInvalidCases() {
        return ALL_INVALID.stream().map(Arguments::of);
    }

    public static Stream<Arguments> e164Cases() {
        return E164_CASES.stream();
    }

    public static Stream<Arguments> displayCases() {
        return DISPLAY_CASES.stream();
    }
}
