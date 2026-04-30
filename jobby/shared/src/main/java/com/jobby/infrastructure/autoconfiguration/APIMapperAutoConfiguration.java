package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.infrastructure.response.implementation.problemdetails.ProblemDetailsResultMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIMapperAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HttpResponseProcessor apiMapper(){
        return new ProblemDetailsResultMapper();
    }
}
