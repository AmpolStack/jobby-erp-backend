package com.jobby.userservice.domain.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTest {
    @Test
    void CreateWhenAllIsNull(){
        // Act
        var response = User.create(0,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        // Assert
        Assertions.assertTrue(response.isFailure());
    }
}
