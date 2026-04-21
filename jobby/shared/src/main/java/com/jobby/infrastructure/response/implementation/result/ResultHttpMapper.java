package com.jobby.infrastructure.response.implementation.result;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.response.definition.APIMapper;
import com.jobby.infrastructure.response.definition.ErrorTypeHttpCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResultHttpMapper implements APIMapper {
    public <T> ResponseEntity<Result<T, Error>> map(
            Result<T, Error> result, HttpStatus successState)
    {

        if(result.isFailure()){
            var statusCode = ErrorTypeHttpCollection.toHttpStatus(result.error().getCode());
            var error = ErrorTypeHttpCollection.toResponseError(result.error());
            return ResponseEntity.status(statusCode).body(Result.failure(error));
        }

        return ResponseEntity.status(successState).body(result);
    }

    public <T> ResponseEntity<Result<T, Error>> map(
            Result<T, Error> result)
    {
        return map(result, HttpStatus.OK);
    }

}
