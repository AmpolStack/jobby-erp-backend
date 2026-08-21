package com.jobby.userservice.infrastructure;

import com.jobby.domain.ports.events.EventRegistry;
import com.jobby.domain.ports.events.EventRoute;
import com.jobby.domain.ports.in.CommandBus;
import com.jobby.domain.ports.in.EventBus;
import com.jobby.domain.ports.in.QueryBus;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.application.contracts.commands.*;
import com.jobby.userservice.application.useCases.*;
import com.jobby.userservice.application.contracts.events.EmailChangeRequestEvent;
import com.jobby.userservice.application.contracts.events.UserCreatedEvent;
import com.jobby.userservice.application.contracts.queries.GetOwnerByIdQuery;
import com.jobby.userservice.events.UserCreatedSchema;
import com.jobby.infrastructure.bus.CommandBusImpl;
import com.jobby.infrastructure.bus.EventBusImpl;
import com.jobby.infrastructure.bus.LoggingCommandBusDecorator;
import com.jobby.infrastructure.bus.LoggingEventBusDecorator;
import com.jobby.infrastructure.bus.LoggingQueryBusDecorator;
import com.jobby.infrastructure.bus.MetricsCommandBusDecorator;
import com.jobby.infrastructure.bus.MetricsEventBusDecorator;
import com.jobby.infrastructure.bus.MetricsQueryBusDecorator;
import com.jobby.infrastructure.bus.QueryBusImpl;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AutoConfiguration {

    @Primary
    @Bean
    public TransformationRegistry defaultTransformationRegistry(){
        return new TransformationRegistry();
    }

    @Bean
    public EventRegistry eventRegistry(){
        return new EventRegistry()
                .register(UserCreatedEvent.class,
                        new EventRoute<>("com.jobby.user.create-user", UserCreatedSchema.class))
                .register(EmailChangeRequestEvent.class,
                        new EventRoute<>("com.jobby.user.email-change-request", Object.class));
    }

    @Bean
    @Primary
    public CommandBus commandBus(
            RequestEmailChangeUseCaseAdapter requestEmailChange,
            ConfirmEmailChangeUseCaseAdapter confirmEmailChange,
            RemoveProfileImageUseCaseAdapter removeProfileImage,
            CreateOwnerUseCaseAdapter createOwner,
            RemoveRecoveryEmailUseCaseAdapter removeRecoveryEmail,
            UpdateProfileImageUseCaseAdapter updateProfileImage,
            UpdateRecoveryEmailUseCaseAdapter updateRecoveryEmail,
            UpdateUserUseCase updateUserUseCase,
            UpdateUserStatusUseCase updateUserStatusUseCase,
            MeterRegistry meterRegistry) {
        var impl = new CommandBusImpl()
                .register(RequestEmailChangeCommand.class, requestEmailChange)
                .register(ConfirmEmailChangeCommand.class, confirmEmailChange)
                .register(RemoveProfileImageCommand.class, removeProfileImage)
                .register(CreateOwnerCommand.class, createOwner)
                .register(RemoveRecoveryEmailCommand.class, removeRecoveryEmail)
                .register(UpdateIProfileImageCommand.class, updateProfileImage)
                .register(UpdateRecoveryEmailCommand.class, updateRecoveryEmail)
                .register(UpdateUserCommand.class, updateUserUseCase)
                .register(UpdateUserStatusCommand.class, updateUserStatusUseCase);
        var withMetrics = new MetricsCommandBusDecorator(impl, meterRegistry);
        return new LoggingCommandBusDecorator(withMetrics);
    }

    @Bean
    @Primary
    public QueryBus queryBus(
            GetOwnerByIdUseCaseAdapter getOwnerById,
            MeterRegistry meterRegistry) {
        var impl = new QueryBusImpl()
                .register(GetOwnerByIdQuery.class, getOwnerById);
        var withMetrics = new MetricsQueryBusDecorator(impl, meterRegistry);
        return new LoggingQueryBusDecorator(withMetrics);
    }

    @Bean
    @Primary
    public EventBus eventBus(
            SendWelcomeEmailUseCaseAdapter sendWelcomeEmail,
            MeterRegistry meterRegistry) {
        var impl = new EventBusImpl()
                .register(UserCreatedEvent.class, sendWelcomeEmail);
        var withMetrics = new MetricsEventBusDecorator(impl, meterRegistry);
        return new LoggingEventBusDecorator(withMetrics);
    }
}
