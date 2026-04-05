package com.jobby.domain.mobility.error;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Error {
    private ErrorType code;
    private Field[] fields;
}
