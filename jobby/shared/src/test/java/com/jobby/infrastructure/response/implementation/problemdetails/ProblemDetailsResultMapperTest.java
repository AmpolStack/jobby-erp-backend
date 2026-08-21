package com.jobby.infrastructure.response.implementation.problemdetails;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.adapter.SupportIdProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProblemDetailsResultMapper - Unit Tests")
class ProblemDetailsResultMapperTest {

    private ProblemDetailsResultMapper mapper;
    private final String testSupportId = "test-trace-id";

    @BeforeEach
    void setUp() {
        var supportIdProvider = new SupportIdProvider(null) {
            @Override
            public Optional<String> current() {
                return Optional.of(testSupportId);
            }
        };
        mapper = new ProblemDetailsResultMapper(supportIdProvider);
    }

    @Test
    @DisplayName("success result returns data with given status")
    void givenSuccessResult_whenMap_returnsDataWithStatus() {
        Result<String, Error> result = Result.success("hello");

        var response = mapper.map(result, HttpStatus.CREATED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("hello");
    }

    @Test
    @DisplayName("success result with default status returns 200 OK")
    void givenSuccessResult_whenMapWithoutStatus_returnsOk() {
        Result<String, Error> result = Result.success("data");

        var response = mapper.map(result);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("data");
    }

    @Test
    @DisplayName("user error result returns ProblemDetails without supportId")
    void givenUserError_whenMap_returnsProblemDetailsWithoutSupportId() {
        var error = new Error(ErrorType.INVALID_INPUT, new Field[]{
                new Field("email", "invalid")
        });
        var result = Result.failure(error);

        var response = mapper.map(result);
        var body = response.getBody();

        assertThat(body).isInstanceOf(ProblemDetails.class);
        var problem = (ProblemDetails) body;
        assertThat(problem.getContext()).doesNotContainKey("supportId");
        assertThat(problem.getTitle()).isEqualTo("Invalid Input");
    }

    @Test
    @DisplayName("ITN error result returns ProblemDetails with supportId")
    void givenItnError_whenMap_returnsProblemDetailsWithSupportId() {
        var error = new Error(ErrorType.ITN_VALIDATION_NULL, new Field[]{
                new Field("f", "d")
        });
        var result = Result.failure(error);

        var response = mapper.map(result);
        var body = (ProblemDetails) response.getBody();

        assertThat(body.getContext()).containsEntry("supportId", testSupportId);
        assertThat(body.getTitle()).isEqualTo("Internal Validation Error");
    }

    @Test
    @DisplayName("ITS error result returns ProblemDetails with supportId")
    void givenItsError_whenMap_returnsProblemDetailsWithSupportId() {
        var error = new Error(ErrorType.ITS_OPERATION_ERROR, new Field[]{
                new Field("f", "d")
        });
        var result = Result.failure(error);

        var response = mapper.map(result);
        var body = (ProblemDetails) response.getBody();

        assertThat(body.getContext()).containsEntry("supportId", testSupportId);
        assertThat(body.getTitle()).isEqualTo("Internal System Error");
    }

    @Test
    @DisplayName("user error returns correct HTTP status")
    void givenUserError_whenMap_returnsCorrectHttpStatus() {
        assertStatus(ErrorType.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        assertStatus(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        assertStatus(ErrorType.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED);
        assertStatus(ErrorType.PERMISSION_DENIED, HttpStatus.FORBIDDEN);
        assertStatus(ErrorType.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("ITN error returns correct HTTP status")
    void givenItnError_whenMap_returnsCorrectHttpStatus() {
        assertStatus(ErrorType.ITN_VALIDATION_NULL, HttpStatus.BAD_REQUEST);
        assertStatus(ErrorType.ITN_VALIDATION_FORMAT, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("ITS error returns correct HTTP status")
    void givenItsError_whenMap_returnsCorrectHttpStatus() {
        assertStatus(ErrorType.ITS_OPERATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        assertStatus(ErrorType.ITS_DB_CONNECTION_FAILED, HttpStatus.SERVICE_UNAVAILABLE);
        assertStatus(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE, HttpStatus.BAD_GATEWAY);
    }

    private void assertStatus(ErrorType type, HttpStatus expected) {
        var error = new Error(type, new Field[]{new Field("f", "d")});
        var result = Result.failure(error);
        var response = mapper.map(result);
        assertThat(response.getStatusCode()).as("Status for " + type).isEqualTo(expected);
    }

    @Test
    @DisplayName("supportId is always fetched from provider on failure")
    void givenAnyError_whenMap_supportIdIsFetched() {
        var captured = new boolean[]{false};
        var provider = new SupportIdProvider(null) {
            @Override
            public Optional<String> current() {
                captured[0] = true;
                return Optional.of("id");
            }
        };
        var testMapper = new ProblemDetailsResultMapper(provider);
        var error = new Error(ErrorType.INVALID_INPUT, new Field[]{new Field("f", "d")});

        testMapper.map(Result.failure(error));

        assertThat(captured[0]).isTrue();
    }
}
