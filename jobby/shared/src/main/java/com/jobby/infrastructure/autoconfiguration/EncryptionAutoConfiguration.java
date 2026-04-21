package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.configurations.EncryptConfig;
import com.jobby.domain.ports.security.encrypt.EncryptBuilder;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.infrastructure.adapter.encrypt.AESEncryptionService;
import com.jobby.infrastructure.adapter.encrypt.DefaultEncryptBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({EncryptBuilder.class, EncryptConfig.class})
public class EncryptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "app.security.encryption")
    @Validated
    public EncryptConfig encryptConfig(){
        return new EncryptConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptBuilder encryptBuilder() {
        return new DefaultEncryptBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptionService encryptionService(EncryptBuilder encryptBuilder, EncryptConfig config) {
        return new AESEncryptionService(encryptBuilder, config);
    }
}
