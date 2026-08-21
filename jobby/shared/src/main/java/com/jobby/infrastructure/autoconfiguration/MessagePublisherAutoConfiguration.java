package com.jobby.infrastructure.autoconfiguration;

import com.jobby.domain.ports.MessagingPublisher;
import com.jobby.domain.ports.events.EventPublisher;
import com.jobby.domain.ports.events.EventRegistry;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.infrastructure.adapter.KafkaMessagingPublisher;
import com.jobby.infrastructure.adapter.events.EventLogger;
import com.jobby.infrastructure.adapter.events.EventPublisherAdapter;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({KafkaTemplate.class, MessagingPublisher.class})
public class MessagePublisherAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MessagingPublisher messagingPublisher(KafkaTemplate<String,Object> kafkaTemplate, ObservationRegistry observationRegistry) {
        return new KafkaMessagingPublisher(kafkaTemplate, observationRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher eventPublisher(MessagingPublisher messagingPublisher,
                                         EventRegistry eventRegistry,
                                         TransformationRegistry transformationRegistry){
        return new EventPublisherAdapter(messagingPublisher,
                eventRegistry,
                transformationRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventLogger eventLogger(ObservationRegistry observationRegistry) {
        return new EventLogger(observationRegistry);
    }
}
