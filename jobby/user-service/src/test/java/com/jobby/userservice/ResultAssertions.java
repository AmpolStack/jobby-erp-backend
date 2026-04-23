package com.jobby.userservice;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.Assertions;

public class ResultAssertions {
    public static <T,E> void assertSuccess(Result<T,E> result){
        Assertions.assertTrue(result.isSuccess());
    }

    public static <T,E> void assertFailure(Result<T,E> obtained){
        Assertions.assertTrue(obtained.isFailure());
    }

    public static <T,T2> void assertFailure(Result<T, Error> obtained, Result<T2,Error> expected){
        Assertions.assertTrue(obtained.isFailure());
        Assertions.assertEquals(expected.error(), obtained.error());
    }
}
