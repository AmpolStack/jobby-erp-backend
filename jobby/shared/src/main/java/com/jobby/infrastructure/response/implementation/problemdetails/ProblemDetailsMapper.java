package com.jobby.infrastructure.response.implementation.problemdetails;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.response.definition.ErrorTypeHttpCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.EnumMap;
import java.util.Map;

public class ProblemDetailsMapper {

    private static final String BASE_URI = "https://api.jobby.com/problems/";
    private static final String ITN_TITLE = "Internal Validation Error";
    private static final String ITN_DETAIL = "An unexpected validation error occurred. This is a system issue. Please contact support.";
    private static final String ITS_TITLE = "Internal System Error";
    private static final String ITS_DETAIL = "A system error occurred. Please try again later or contact support.";

    private static final Map<ErrorType, String> TITLES = new EnumMap<>(ErrorType.class);
    private static final Map<ErrorType, String> DETAILS = new EnumMap<>(ErrorType.class);

    static {
        TITLES.put(ErrorType.INVALID_INPUT, "Invalid Input");
        TITLES.put(ErrorType.USER_NOT_FOUND, "User Not Found");
        TITLES.put(ErrorType.NOT_FOUND, "Resource Not Found");
        TITLES.put(ErrorType.AUTHENTICATION_FAILED, "Authentication Failed");
        TITLES.put(ErrorType.PERMISSION_DENIED, "Permission Denied");
        TITLES.put(ErrorType.RESOURCE_ALREADY_EXISTS, "Resource Already Exists");
        TITLES.put(ErrorType.VALIDATION_ERROR, "Validation Error");
        TITLES.put(ErrorType.UNSUPPORTED_OPERATION, "Unsupported Operation");
        TITLES.put(ErrorType.TOKEN_EXPIRED, "Token Expired");
        TITLES.put(ErrorType.TOKEN_INVALID, "Token Invalid");
        TITLES.put(ErrorType.EMPLOYEE_INACTIVE, "Employee Inactive");

        DETAILS.put(ErrorType.INVALID_INPUT, "The provided input is invalid.");
        DETAILS.put(ErrorType.USER_NOT_FOUND, "The requested user could not be found.");
        DETAILS.put(ErrorType.NOT_FOUND, "The requested resource could not be found.");
        DETAILS.put(ErrorType.AUTHENTICATION_FAILED, "The provided credentials are invalid.");
        DETAILS.put(ErrorType.PERMISSION_DENIED, "You do not have permission to access this resource.");
        DETAILS.put(ErrorType.RESOURCE_ALREADY_EXISTS, "The resource already exists.");
        DETAILS.put(ErrorType.VALIDATION_ERROR, "The request data failed validation.");
        DETAILS.put(ErrorType.UNSUPPORTED_OPERATION, "The requested operation is not supported.");
        DETAILS.put(ErrorType.TOKEN_EXPIRED, "The authentication token has expired.");
        DETAILS.put(ErrorType.TOKEN_INVALID, "The authentication token is invalid or malformed.");
        DETAILS.put(ErrorType.EMPLOYEE_INACTIVE, "The employee account is inactive.");
    }

    public static ResponseEntity<ProblemDetails> toProblemDetails(Error error, String supportId) {
        ErrorType errorType = error.getCode();
        HttpStatus status = ErrorTypeHttpCollection.toHttpStatus(errorType);

        Error sanitizedError = ErrorTypeHttpCollection.toSanitizedError(error);

        var builder = ProblemDetails.builder()
                .type(BASE_URI + errorType.name().toLowerCase().replace('_', '-'))
                .title(resolveTitle(errorType))
                .status(status.value())
                .detail(resolveDetail(errorType))
                .errors(sanitizedError.getFields())
                .addContext("errorCode", errorType.name());

        if (supportId != null && ErrorTypeHttpCollection.isInternalSystemError(errorType)) {
            builder.addContext("supportId", supportId);
        }

        if (supportId != null && ErrorTypeHttpCollection.isInternalValidationError(errorType)) {
            builder.addContext("supportId", supportId);
        }

        ProblemDetails problem = builder.build();

        return ResponseEntity.status(status)
                .header("Content-Type", "application/problem+json")
                .body(problem);
    }

    private static String resolveTitle(ErrorType errorType) {
        if (ErrorTypeHttpCollection.isInternalValidationError(errorType)) {
            return ITN_TITLE;
        }
        if (ErrorTypeHttpCollection.isInternalSystemError(errorType)) {
            return ITS_TITLE;
        }
        return TITLES.getOrDefault(errorType, "Bad Request");
    }

    private static String resolveDetail(ErrorType errorType) {
        if (ErrorTypeHttpCollection.isInternalValidationError(errorType)) {
            return ITN_DETAIL;
        }
        if (ErrorTypeHttpCollection.isInternalSystemError(errorType)) {
            return ITS_DETAIL;
        }
        return DETAILS.getOrDefault(errorType, "The request could not be processed.");
    }
}
