package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.transaction.TransactionalContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public TransactionalContext persistenceTransactionContext() {
        return new TransactionalContext();
    }
}
