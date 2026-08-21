package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;
import java.util.List;
import java.util.stream.Stream;

public class EmailBoundaries {

    private static final List<String> VALID_VALUES = List.of(
            "user@example.com",
            "user.name+tag@sub.domain.org",
            "USER@EXAMPLE.COM",
            "u@e.co"
    );

    private static final List<String> INVALID_FORMAT_VALUES = List.of(
            "not-an-email",
            "missing@tld",
            "@nodomain.com",
            "no-at-sign.com",
            "spaces in@email.com",
            "double@@at.com"
    );

    public static final List<String> ALL_INVALID = Stream.concat(
            NullityBoundaries.BLANK_VALUES.stream(),
            INVALID_FORMAT_VALUES.stream()
    ).toList();

    public static String getDescription(String email) {
        if (email == null) return "null";
        if (email.isBlank()) return "blank -> '" + email.replace("\t", "\\t").replace("\n", "\\n") + "'";
        return email;
    }

    public static Stream<Arguments> validCases() {
        return VALID_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidFormatCases() {
        return INVALID_FORMAT_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidFormatCasesWithName() {
        return INVALID_FORMAT_VALUES.stream()
                .map(v -> Arguments.of(v, v));
    }

    public static Stream<Arguments> allInvalidCases() {
        return ALL_INVALID.stream().map(Arguments::of);
    }
}
