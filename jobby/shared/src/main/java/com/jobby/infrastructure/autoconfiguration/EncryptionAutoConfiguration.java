package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.domain.ports.security.encrypt.EncryptBuilder;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.infrastructure.adapter.encrypt.AESEncryptionService;
import com.jobby.infrastructure.adapter.encrypt.DefaultEncryptBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({EncryptBuilder.class})
public class EncryptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EncryptBuilder encryptBuilder() {
        return new DefaultEncryptBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptionService encryptionService(
            SafeResultValidator safeResultValidator,
            EncryptBuilder encryptBuilder) {
        return new AESEncryptionService(safeResultValidator, encryptBuilder);
    }
}
