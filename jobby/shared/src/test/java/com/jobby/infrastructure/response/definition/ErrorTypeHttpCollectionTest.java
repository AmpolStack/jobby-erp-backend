package com.jobby.infrastructure.response.definition;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorTypeHttpCollection - Unit Tests")
class ErrorTypeHttpCollectionTest {

    @Test
    @DisplayName("toHttpStatus: user errors return correct status")
    void givenUserError_whenToHttpStatus_returnsCorrectStatus() {
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.INVALID_INPUT)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.USER_NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.AUTHENTICATION_FAILED)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.PERMISSION_DENIED)).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.RESOURCE_ALREADY_EXISTS)).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.VALIDATION_ERROR)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.UNSUPPORTED_OPERATION)).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.TOKEN_EXPIRED)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.TOKEN_INVALID)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.EMPLOYEE_INACTIVE)).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("toHttpStatus: ITN errors return correct status")
    void givenInternalValidationError_whenToHttpStatus_returnsCorrectStatus() {
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_NULL)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_BLANK)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_EMPTY)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_FORMAT)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_RANGE)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_TYPE)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITN_VALIDATION_CUSTOM)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("toHttpStatus: ITS errors return correct status")
    void givenInternalSystemError_whenToHttpStatus_returnsCorrectStatus() {
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_INVALID_OPTION_PARAMETER)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_OPERATION_ERROR)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_INVALID_STATE)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_DB_CONNECTION_FAILED)).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE)).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_CONFIGURATION_ERROR)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_SERIALIZATION_ERROR)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorTypeHttpCollection.toHttpStatus(ErrorType.ITS_UNKNOWN_ERROR)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("toHttpStatus: unknown type returns INTERNAL_SERVER_ERROR")
    void givenUnknownType_whenToHttpStatus_returnsInternalServerError() {
        assertThat(ErrorTypeHttpCollection.toHttpStatus(null)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("isUserError: user error types return true")
    void givenUserErrorType_whenIsUserError_returnsTrue() {
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.INVALID_INPUT)).isTrue();
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.VALIDATION_ERROR)).isTrue();
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.PERMISSION_DENIED)).isTrue();
    }

    @Test
    @DisplayName("isUserError: ITN types return false")
    void givenItnType_whenIsUserError_returnsFalse() {
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.ITN_VALIDATION_NULL)).isFalse();
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.ITN_VALIDATION_CUSTOM)).isFalse();
    }

    @Test
    @DisplayName("isUserError: ITS types return false")
    void givenItsType_whenIsUserError_returnsFalse() {
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.ITS_OPERATION_ERROR)).isFalse();
        assertThat(ErrorTypeHttpCollection.isUserError(ErrorType.ITS_DB_CONNECTION_FAILED)).isFalse();
    }

    @Test
    @DisplayName("isInternalValidationError: ITN types return true")
    void givenItnType_whenIsInternalValidationError_returnsTrue() {
        assertThat(ErrorTypeHttpCollection.isInternalValidationError(ErrorType.ITN_VALIDATION_NULL)).isTrue();
        assertThat(ErrorTypeHttpCollection.isInternalValidationError(ErrorType.ITN_VALIDATION_BLANK)).isTrue();
        assertThat(ErrorTypeHttpCollection.isInternalValidationError(ErrorType.ITN_VALIDATION_CUSTOM)).isTrue();
    }

    @Test
    @DisplayName("isInternalValidationError: user and ITS types return false")
    void givenNonItnType_whenIsInternalValidationError_returnsFalse() {
        assertThat(ErrorTypeHttpCollection.isInternalValidationError(ErrorType.INVALID_INPUT)).isFalse();
        assertThat(ErrorTypeHttpCollection.isInternalValidationError(ErrorType.ITS_OPERATION_ERROR)).isFalse();
    }

    @Test
    @DisplayName("isInternalSystemError: ITS types return true")
    void givenItsType_whenIsInternalSystemError_returnsTrue() {
        assertThat(ErrorTypeHttpCollection.isInternalSystemError(ErrorType.ITS_OPERATION_ERROR)).isTrue();
        assertThat(ErrorTypeHttpCollection.isInternalSystemError(ErrorType.ITS_DB_CONNECTION_FAILED)).isTrue();
    }

    @Test
    @DisplayName("isInternalSystemError: user and ITN types return false")
    void givenNonItsType_whenIsInternalSystemError_returnsFalse() {
        assertThat(ErrorTypeHttpCollection.isInternalSystemError(ErrorType.INVALID_INPUT)).isFalse();
        assertThat(ErrorTypeHttpCollection.isInternalSystemError(ErrorType.ITN_VALIDATION_NULL)).isFalse();
    }

    @Test
    @DisplayName("toSanitizedError: user error returns original error unchanged")
    void givenUserError_whenToSanitizedError_returnsOriginal() {
        var original = new Error(ErrorType.INVALID_INPUT, new Field[]{
                new Field("email", "invalid format")
        });

        var sanitized = ErrorTypeHttpCollection.toSanitizedError(original);

        assertThat(sanitized).isSameAs(original);
    }

    @Test
    @DisplayName("toSanitizedError: ITN error replaces fields with sanitized message")
    void givenItnError_whenToSanitizedError_replacesFields() {
        var original = new Error(ErrorType.ITN_VALIDATION_NULL, new Field[]{
                new Field("someField", "some internal detail")
        });

        var sanitized = ErrorTypeHttpCollection.toSanitizedError(original);

        assertThat(sanitized.getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        assertThat(sanitized.getFields()).hasSize(1);
        assertThat(sanitized.getFields()[0].getInstance()).isEqualTo("internal");
        assertThat(sanitized.getFields()[0].getReason()).contains("system issue");
    }

    @Test
    @DisplayName("toSanitizedError: ITS error replaces fields with sanitized message")
    void givenItsError_whenToSanitizedError_replacesFields() {
        var original = new Error(ErrorType.ITS_OPERATION_ERROR, new Field[]{
                new Field("db", "connection pool exhausted")
        });

        var sanitized = ErrorTypeHttpCollection.toSanitizedError(original);

        assertThat(sanitized.getCode()).isEqualTo(ErrorType.ITS_OPERATION_ERROR);
        assertThat(sanitized.getFields()).hasSize(1);
        assertThat(sanitized.getFields()[0].getInstance()).isEqualTo("system");
        assertThat(sanitized.getFields()[0].getReason()).contains("try again later");
    }
}
