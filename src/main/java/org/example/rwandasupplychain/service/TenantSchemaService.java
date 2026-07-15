package org.example.rwandasupplychain.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
public class TenantSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createTenantSchema(String tenantId) {
        // 1. Create the schema if it doesn't exist
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);

        // 2. Load the SQL script
        String ddl = loadDdlScript();

        // 3. Replace placeholder with the actual tenant schema name
        String sql = ddl.replace("${schema}", tenantId);

        // 4. Execute each statement separately (split on ';')
        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }

    private String loadDdlScript() {
        try {
            ClassPathResource resource = new ClassPathResource("Schema.sql");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema.sql from classpath", e);
        }
    }
}