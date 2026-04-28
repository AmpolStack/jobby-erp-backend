package com.jobby.userservice;

import org.junit.jupiter.params.provider.Arguments;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class NullityOps {

    public static final List<String> BLANK_VALUES = Arrays.asList(null, "", "    ", "\t", "\n");

    public static String getNullityName(String value) {
        return switch (value){
            case null -> "null";
            case "" -> "empty string -> ''";
            case "    " -> "spaces -> '   '";
            case "\t" -> "tab -> '/t'";
            case "\n" -> "newline -> '/n'";
            default -> "other -> [" + value + " ]";
        };
    }

    public static String getNullityName(Object object){
        if(object instanceof String){
            return getNullityName(object);
        }
        return "null";
    }

    public static Stream<Arguments> getBlankCases(BiFunction<String, String, Arguments[]> bifunction){
        return BLANK_VALUES.stream()
                .flatMap(blank -> Stream.of(
                        bifunction.apply(blank, getNullityName(blank)
                        )));
    }
}
