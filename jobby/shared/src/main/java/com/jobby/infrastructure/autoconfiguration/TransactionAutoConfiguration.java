package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.transaction.TransactionOrchestratorAdapter;
import com.jobby.infrastructure.transaction.SpringDataTransactionalContext;
import com.jobby.infrastructure.transaction.proxy.MongoDbSpringDataHandler;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringDataTransactionalContext persistenceTransactionContext() {
        return new SpringDataTransactionalContext();
    }

    @Bean("defaultPersistenceProxy")
    @ConditionalOnMissingBean
    public PersistenceProxy defaultPersistenceProxy() {
        return new MongoDbSpringDataHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionOrchestratorAdapter transactionExecutor(
            SpringDataTransactionalContext context,
            PersistenceProxy persistenceProxy) {
        return new TransactionOrchestratorAdapter(context, persistenceProxy);
    }
}
