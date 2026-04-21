package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.configurations.MacConfig;
import com.jobby.domain.ports.security.hashing.mac.MacBuilder;
import com.jobby.domain.ports.security.hashing.mac.MacService;
import com.jobby.infrastructure.adapter.hashing.mac.DefaultMacBuilder;
import com.jobby.infrastructure.adapter.hashing.mac.HmacSha256Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MacService.class})
public class MacAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "app.security.mac")
    @Validated
    public MacConfig macConfig(){
        return new MacConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public MacBuilder macBuilder() {
        return new DefaultMacBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public MacService macService(
            MacBuilder macBuilder,
            MacConfig config) {
        return new HmacSha256Service(macBuilder, config);
    }

}

