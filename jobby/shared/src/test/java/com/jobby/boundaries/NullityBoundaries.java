package com.jobby.boundaries;

import org.junit.jupiter.params.provider.Arguments;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class NullityBoundaries {

    public static final List<String> BLANK_VALUES = Arrays.asList(null, "", "    ", "\t", "\n");

    private static String getNullityName(String value) {
        return switch (value){
            case null -> "null";
            case "" -> "empty string -> ''";
            case "    " -> "spaces -> '   '";
            case "\t" -> "tab -> '/t'";
            case "\n" -> "newline -> '/n'";
            default -> "other -> [" + value + " ]";
        };
    }

    private static String getNullityName(Object object){
        if(object instanceof String){
            return getNullityName(object);
        }
        return "null";
    }

    public static Stream<Arguments> get(){
        return BLANK_VALUES.stream()
                .flatMap(blank -> Stream.of(
                        Arguments.of(blank)
                        ));
    }

    public static Stream<Arguments> getWithLabels(){
        return BLANK_VALUES.stream()
                .flatMap(blank -> Stream.of(
                        Arguments.of(blank, getNullityName(blank))
                ));
    }

    public static Stream<Arguments> getWithoutNull(){
        return BLANK_VALUES.stream()
                .filter(Objects::nonNull)
                .flatMap(blank -> Stream.of(
                        Arguments.of(blank, getNullityName(blank))
                ));
    }

    public static Stream<Arguments> get(BiFunction<String, String, Arguments[]> bifunction){
        return BLANK_VALUES.stream()
                .flatMap(blank -> Stream.of(
                        bifunction.apply(blank, getNullityName(blank)
                        )));
    }
}
