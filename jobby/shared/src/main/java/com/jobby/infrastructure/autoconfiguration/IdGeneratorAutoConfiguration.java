package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.configurations.IdConfig;
import com.jobby.infrastructure.adapter.SnowflakeIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SnowflakeIdGenerator.class})
public class IdGeneratorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "app.id")
    public IdConfig idConfig(){
        return new IdConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator(IdConfig idConfig){
        return new SnowflakeIdGenerator(idConfig);
    }
}