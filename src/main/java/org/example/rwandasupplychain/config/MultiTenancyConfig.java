package org.example.rwandasupplychain.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiTenancyConfig {

    private final DataSource dataSource;
    private final JpaProperties jpaProperties;
    private final MultiTenantConnectionProvider multiTenantConnectionProvider;
    private final CurrentTenantIdentifierResolver<String> tenantIdentifierResolver;

    public MultiTenancyConfig(DataSource dataSource,
                              JpaProperties jpaProperties,
                              MultiTenantConnectionProvider multiTenantConnectionProvider,
                              CurrentTenantIdentifierResolver<String> tenantIdentifierResolver) {
        this.dataSource = dataSource;
        this.jpaProperties = jpaProperties;
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        // Set multi-tenancy provider and resolver (these constants exist)
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
        // Use string literal for the strategy (works in all Hibernate versions)
        properties.put("hibernate.multi_tenant", "SCHEMA");

        return builder
                .dataSource(dataSource)
                .packages("org.example.rwandasupplychain.Entities")
                .persistenceUnit("default")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}