package com.jobby.infrastructure.autoconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobby.domain.ports.CacheService;
import com.jobby.infrastructure.adapter.RedisCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheAutoConfiguration {
    @Bean()
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.data.redis")
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration();
    }

    @Bean()
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory(
            RedisStandaloneConfiguration redisStandaloneConfiguration
    ) {
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean()
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> primaryRedisTemplate(
            RedisConnectionFactory redisConnectionFactory
    ){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        var serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheService cacheService(RedisTemplate<String, Object> primaryRedisTemplate, ObjectMapper objectMapper) {
        return new RedisCacheService(primaryRedisTemplate, objectMapper);
    }
}
