package com.jobby.infrastructure.response.implementation.problemdetails;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.adapter.SupportIdProvider;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
public class ProblemDetailsResultMapper implements HttpResponseProcessor {

    private final SupportIdProvider supportIdProvider;
    
    public <T> ResponseEntity<?> map(Result<T, Error> result, HttpStatus successStatus) {
        if (result.isFailure()) {
            var supportId = supportIdProvider.current().orElse("unavailable");
            return ProblemDetailsMapper.toProblemDetails(result.error(), supportId);
        }
        return ResponseEntity.status(successStatus).body(result.data());
    }

    public <T> ResponseEntity<?> map(Result<T, Error> result) {
        return map(result, HttpStatus.OK);
    }
}