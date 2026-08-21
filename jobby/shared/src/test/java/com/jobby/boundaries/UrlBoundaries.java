package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;
import java.util.List;
import java.util.stream.Stream;

public class UrlBoundaries {

    public static final List<String> VALID_VALUES = List.of(
            "https://cdn.example.com/image.jpg",
            "https://cdn.example.com/image.jpeg",
            "https://cdn.example.com/image.png",
            "https://cdn.example.com/image.webp",
            "https://cdn.example.com/path/to/image.png"
    );

    public static final List<String> INVALID_URL_VALUES = List.of(
            "http://example.com/image.jpg",
            "ftp://example.com/image.jpg",
            "not-a-url",
            "https://",
            "https://no-tld/image.jpg"
    );

    public static final List<String> INVALID_EXTENSION_VALUES = List.of(
            "https://cdn.example.com/image.gif",
            "https://cdn.example.com/image.bmp",
            "https://cdn.example.com/image.svg",
            "https://cdn.example.com/image.tiff",
            "https://cdn.example.com/image.pdf",
            "https://cdn.example.com/image.exe"
    );

    public static final List<String> ALL_INVALID = Stream.of(
            NullityBoundaries.BLANK_VALUES.stream(),
            INVALID_URL_VALUES.stream(),
            INVALID_EXTENSION_VALUES.stream()
    ).flatMap(s -> s).toList();

    public static String getDescription(String url) {
        if (url == null) return "null";
        if (url.isBlank()) return "blank";
        return url;
    }

    public static Stream<Arguments> validCases() {
        return VALID_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidUrlCases() {
        return INVALID_URL_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> invalidExtensionCases() {
        return INVALID_EXTENSION_VALUES.stream().map(Arguments::of);
    }

    public static Stream<Arguments> allInvalidCases() {
        return ALL_INVALID.stream().map(Arguments::of);
    }
}
