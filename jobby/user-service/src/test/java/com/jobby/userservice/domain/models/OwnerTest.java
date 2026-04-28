package com.jobby.userservice.domain.models;

import com.jobby.userservice.ResultAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

public class OwnerTest {

    @Nested
    class CreateMethod{

        @ParameterizedTest
        @MethodSource("casesOfCreate")
        @DisplayName("Given all fields are correct, when create method is calling, returns success")
        void create_WhenAllIsCorrect_ShouldReturnsSuccess(
                long id,
                long userId,
                Map<String, String> secureParameters
        ){
            // Act
            var result = Owner.create(id, userId, secureParameters);

            // Arrange
            ResultAssertions.assertSuccess(result);
            Assertions.assertSame(id, result.data().getId());
            Assertions.assertSame(userId, result.data().getUserId());
            Assertions.assertNotNull(result.data().getCreatedAt());
            Assertions.assertNotNull(result.data().getModifiedAt());
            Assertions.assertNull(result.data().getAlternativeEmail());

            if(secureParameters == null) Assertions.assertNull(result.data().getSecureParameters());
            else Assertions.assertEquals(secureParameters, result.data().getSecureParameters());
        }

        private static Stream<Arguments> casesOfCreate(){
            return Stream.of(
                    Arguments.of(1L, 1L, null),
                    Arguments.of(2L, 2L, Map.of("2fa", "active", "ops-pin", "active")),
                    Arguments.of(3L, 3L, Map.of("2fa", "active", "ops-pin", "active", "classified", "true")),
                    Arguments.of(4L, 4L, null)
            );
        }
    }

    @Nested
    class ReconstructMethod{
        @ParameterizedTest
        @MethodSource("casesOfReconstruct")
        @DisplayName("When reconstruct method is calling, returns success and always sets all fields")
        void create_WhenAllIsCorrect_ShouldReturnsSuccess(
                long id,
                long userId,
                String alternativeEmail,
                Map<String, String> secureParameters,
                Instant createdAt,
                Instant modifiedAt
        ){
            // Act
            var result = Owner.reconstruct(id,
                    userId,
                    alternativeEmail,
                    secureParameters,
                    createdAt,
                    modifiedAt);

            // Arrange
            Assertions.assertNotNull(result);
            Assertions.assertEquals(id, result.getId());
            Assertions.assertEquals(userId, result.getUserId());
            Assertions.assertSame(createdAt, result.getCreatedAt());
            Assertions.assertSame(modifiedAt, result.getModifiedAt());
            Assertions.assertSame(alternativeEmail, result.getAlternativeEmail().getEmail());
            Assertions.assertSame(secureParameters, result.getSecureParameters());
        }

        private static Stream<Arguments> casesOfReconstruct(){
            return Stream.of(
                    Arguments.of(1L, 1L, null, null, null, null),
                    Arguments.of(2L, 2L, "testing@gmail.com", Map.of("2fa", "active", "ops-pin", "active"), null, null),
                    Arguments.of(3L, 3L, "testing2@gmail.com", Map.of("2fa", "active", "ops-pin", "active", "classified", "true"), Instant.now(), Instant.now()),
                    Arguments.of(4L, 4L, null, null, LocalDateTime.of(2026, 12, 2, 3, 2).toInstant(ZoneOffset.UTC), LocalDateTime.of(2024, 11, 2, 3, 2).toInstant(ZoneOffset.UTC))
            );
        }
    }
}
