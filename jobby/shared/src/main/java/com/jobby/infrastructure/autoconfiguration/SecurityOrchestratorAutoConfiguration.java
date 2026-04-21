package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.domain.ports.security.hashing.mac.MacService;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SecurityOrchestrator.class})
public class SecurityOrchestratorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SecurityOrchestrator securityOrchestrator(EncryptionService encryptionService,
                                                     MacService macService) {
        return new SecurityOrchestrator(encryptionService, macService);
    }
}
