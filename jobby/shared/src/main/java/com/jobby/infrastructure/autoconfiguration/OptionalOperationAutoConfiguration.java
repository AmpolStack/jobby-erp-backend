package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.OptionalOperation;
import com.jobby.infrastructure.adapter.OptionalOperationAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OptionalOperationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public OptionalOperation getOptionalOperation(){
        return new OptionalOperationAdapter();
    }
}
