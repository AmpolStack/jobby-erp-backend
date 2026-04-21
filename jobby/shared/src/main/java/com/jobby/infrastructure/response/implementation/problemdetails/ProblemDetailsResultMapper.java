package com.jobby.infrastructure.response.implementation.problemdetails;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.response.definition.APIMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ProblemDetailsResultMapper implements APIMapper {
    
    public <T> ResponseEntity<?> map(Result<T, Error> result, HttpStatus successStatus) {
        if (result.isFailure()) {
            return ProblemDetailsMapper.toProblemDetails(result.error());
        }
        return ResponseEntity.status(successStatus).body(result.data());
    }

    public <T> ResponseEntity<?> map(Result<T, Error> result) {
        return map(result, HttpStatus.OK);
    }
}
