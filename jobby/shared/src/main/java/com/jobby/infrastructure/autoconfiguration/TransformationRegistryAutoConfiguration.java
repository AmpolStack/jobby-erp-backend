package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.transformations.TransformationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({TransformationRegistry.class})
public class TransformationRegistryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransformationRegistry defualtTransformationRegistry(){
        return new TransformationRegistry();
    }

}
