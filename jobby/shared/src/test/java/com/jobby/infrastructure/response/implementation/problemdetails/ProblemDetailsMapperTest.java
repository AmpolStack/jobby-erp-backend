package com.jobby.infrastructure.response.implementation.problemdetails;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProblemDetailsMapper - Unit Tests")
class ProblemDetailsMapperTest {

    private static final String SUPPORT_ID = "trace-123";

    @Test
    @DisplayName("user error includes specific title, detail and no supportId")
    void givenUserError_whenToProblemDetails_hasSpecificTitleAndNoSupportId() {
        var error = new Error(ErrorType.INVALID_INPUT, new Field[]{
                new Field("email", "must be a valid email")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, null);
        var body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Invalid Input");
        assertThat(body.getDetail()).isEqualTo("The provided input is invalid.");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors()[0].getInstance()).isEqualTo("email");
        assertThat(body.getContext()).doesNotContainKey("supportId");
    }

    @Test
    @DisplayName("user error passes original fields through")
    void givenUserError_whenToProblemDetails_passesOriginalFields() {
        var error = new Error(ErrorType.VALIDATION_ERROR, new Field[]{
                new Field("name", "is required"),
                new Field("age", "must be positive")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, null);
        var body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getErrors()).hasSize(2);
    }

    @Test
    @DisplayName("user error: supportId is ignored when null")
    void givenUserErrorAndNullSupportId_whenToProblemDetails_noSupportIdInContext() {
        var error = new Error(ErrorType.NOT_FOUND, new Field[]{
                new Field("id", "not found")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, null);
        var body = response.getBody();

        assertThat(body.getContext()).doesNotContainKey("supportId");
    }

    @Test
    @DisplayName("all user error types have correct titles and details")
    void givenAllUserErrorTypes_whenToProblemDetails_hasCorrectTitleAndDetail() {
        var map = Map.ofEntries(
                entry(ErrorType.INVALID_INPUT, "Invalid Input"),
                entry(ErrorType.USER_NOT_FOUND, "User Not Found"),
                entry(ErrorType.NOT_FOUND, "Resource Not Found"),
                entry(ErrorType.AUTHENTICATION_FAILED, "Authentication Failed"),
                entry(ErrorType.PERMISSION_DENIED, "Permission Denied"),
                entry(ErrorType.RESOURCE_ALREADY_EXISTS, "Resource Already Exists"),
                entry(ErrorType.VALIDATION_ERROR, "Validation Error"),
                entry(ErrorType.UNSUPPORTED_OPERATION, "Unsupported Operation"),
                entry(ErrorType.TOKEN_EXPIRED, "Token Expired"),
                entry(ErrorType.TOKEN_INVALID, "Token Invalid"),
                entry(ErrorType.EMPLOYEE_INACTIVE, "Employee Inactive")
        );

        for (var entry : map.entrySet()) {
            var error = new Error(entry.getKey(), new Field[]{new Field("f", "d")});
            var response = ProblemDetailsMapper.toProblemDetails(error, null);
            var body = response.getBody();

            assertThat(body.getTitle()).as("Title for " + entry.getKey()).isEqualTo(entry.getValue());
        }
    }

    @Test
    @DisplayName("ITN error has sanitized title, detail and includes supportId")
    void givenItnError_whenToProblemDetails_hasSanitizedTitleAndSupportId() {
        var error = new Error(ErrorType.ITN_VALIDATION_NULL, new Field[]{
                new Field("internalField", "sensitive detail")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
        var body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Internal Validation Error");
        assertThat(body.getDetail()).isEqualTo("An unexpected validation error occurred. This is a system issue. Please contact support.");
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors()[0].getInstance()).isEqualTo("internal");
        assertThat(body.getContext()).containsEntry("supportId", SUPPORT_ID);
        assertThat(body.getContext()).containsEntry("errorCode", "ITN_VALIDATION_NULL");
    }

    @Test
    @DisplayName("ITS error has sanitized title, detail and includes supportId")
    void givenItsError_whenToProblemDetails_hasSanitizedTitleAndSupportId() {
        var error = new Error(ErrorType.ITS_DB_CONNECTION_FAILED, new Field[]{
                new Field("db", "connection refused")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
        var body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Internal System Error");
        assertThat(body.getDetail()).isEqualTo("A system error occurred. Please try again later or contact support.");
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors()[0].getInstance()).isEqualTo("system");
        assertThat(body.getContext()).containsEntry("supportId", SUPPORT_ID);
        assertThat(body.getContext()).containsEntry("errorCode", "ITS_DB_CONNECTION_FAILED");
    }

    @Test
    @DisplayName("ITS error with SERVICE_UNAVAILABLE sets correct status")
    void givenItsServiceUnavailable_whenToProblemDetails_setsCorrectStatus() {
        var error = new Error(ErrorType.ITS_DB_CONNECTION_FAILED, new Field[]{
                new Field("db", "down")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
        var body = response.getBody();

        assertThat(body.getStatus()).isEqualTo(503);
    }

    @Test
    @DisplayName("ITS error with BAD_GATEWAY sets correct status")
    void givenItsBadGateway_whenToProblemDetails_setsCorrectStatus() {
        var error = new Error(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE, new Field[]{
                new Field("svc", "timeout")
        });

        var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
        var body = response.getBody();

        assertThat(body.getStatus()).isEqualTo(502);
    }

    @Test
    @DisplayName("all ITN types have sanitized response")
    void givenAllItnTypes_whenToProblemDetails_hasSanitizedResponse() {
        for (var type : ErrorType.values()) {
            if (!type.name().startsWith("ITN")) continue;

            var error = new Error(type, new Field[]{new Field("f", "d")});
            var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
            var body = response.getBody();

            assertThat(body.getTitle()).as("ITN title for " + type).isEqualTo("Internal Validation Error");
            assertThat(body.getContext()).containsKey("supportId");
        }
    }

    @Test
    @DisplayName("all ITS types have sanitized response")
    void givenAllItsTypes_whenToProblemDetails_hasSanitizedResponse() {
        for (var type : ErrorType.values()) {
            if (!type.name().startsWith("ITS")) continue;

            var error = new Error(type, new Field[]{new Field("f", "d")});
            var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
            var body = response.getBody();

            assertThat(body.getTitle()).as("ITS title for " + type).isEqualTo("Internal System Error");
            assertThat(body.getContext()).containsKey("supportId");
        }
    }

    @Test
    @DisplayName("response has Content-Type application/problem+json header")
    void givenAnyError_whenToProblemDetails_hasCorrectContentType() {
        var error = new Error(ErrorType.VALIDATION_ERROR, new Field[]{new Field("f", "d")});

        var response = ProblemDetailsMapper.toProblemDetails(error, null);

        assertThat(response.getHeaders().get("Content-Type"))
                .contains("application/problem+json");
    }

    @Test
    @DisplayName("errorCode is always present in context")
    void givenAnyError_whenToProblemDetails_includesErrorCode() {
        var error = new Error(ErrorType.PERMISSION_DENIED, new Field[]{new Field("f", "d")});

        var response = ProblemDetailsMapper.toProblemDetails(error, null);
        var body = response.getBody();

        assertThat(body.getContext()).containsEntry("errorCode", "PERMISSION_DENIED");
    }

    @Test
    @DisplayName("type URI follows expected pattern")
    void givenAnyError_whenToProblemDetails_typeMatchesPattern() {
        var error = new Error(ErrorType.ITS_DB_CONNECTION_FAILED, new Field[]{new Field("f", "d")});

        var response = ProblemDetailsMapper.toProblemDetails(error, SUPPORT_ID);
        var body = response.getBody();

        assertThat(body.getType().toString())
                .isEqualTo("https://api.jobby.com/problems/its-db-connection-failed");
    }
}
