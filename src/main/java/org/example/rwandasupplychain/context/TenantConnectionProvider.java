package org.example.rwandasupplychain.context;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        if (tenantIdentifier != null && !"public".equals(tenantIdentifier)) {
            connection.setSchema(tenantIdentifier);
        }
        return connection;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {

    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        if (tenantIdentifier != null && !"public".equals(tenantIdentifier)) {
            connection.setSchema("public");
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}