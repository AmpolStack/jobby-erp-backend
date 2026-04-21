package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.response.definition.APIMapper;
import com.jobby.infrastructure.response.implementation.problemdetails.ProblemDetailsResultMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIMapperAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public APIMapper apiMapper(){
        return new ProblemDetailsResultMapper();
    }
}
