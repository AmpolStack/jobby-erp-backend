package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.security.hashing.HashingService;
import com.jobby.infrastructure.adapter.hashing.BcryptHashingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({HashingService.class})
public class HashingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HashingService hashingService() {
        return new BcryptHashingService();
    }
}
