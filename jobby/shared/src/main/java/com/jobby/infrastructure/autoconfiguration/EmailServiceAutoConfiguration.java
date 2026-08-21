package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.EmailService;
import com.jobby.infrastructure.adapter.EmailServiceAdapter;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@ConditionalOnClass({EmailServiceAutoConfiguration.class})
public class EmailServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public EmailService getEmailService(JavaMailSender mailSender, ObservationRegistry observationRegistry){
        return new EmailServiceAdapter(mailSender, observationRegistry);
    }
}
