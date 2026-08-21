package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.events.EventRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({EventRegistry.class})
public class EventRegistryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public EventRegistry eventRegistry(){
        return new EventRegistry();
    }
}
