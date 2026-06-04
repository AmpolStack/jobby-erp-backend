package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.adapter.SupportIdProvider;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.infrastructure.response.implementation.problemdetails.ProblemDetailsResultMapper;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIMapperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SupportIdProvider supportIdProvider(Tracer tracer){
        return new SupportIdProvider(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpResponseProcessor apiMapper(SupportIdProvider supportIdProvider){
        return new ProblemDetailsResultMapper(supportIdProvider);
    }
}
