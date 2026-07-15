package org.example.rwandasupplychain.config;

import org.example.rwandasupplychain.service.TenantSchemaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TenantInitializer implements CommandLineRunner {

    private final TenantSchemaService tenantSchemaService;

    public TenantInitializer(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a default tenant at startup
        tenantSchemaService.createTenantSchema("tenant_test");
        System.out.println("✅ Tenant schema 'tenant_test' created successfully.");
    }
}