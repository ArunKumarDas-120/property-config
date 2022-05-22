package com.property.source.config.model;

import org.springframework.core.io.AbstractFileResolvingResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DataBaseResource extends AbstractFileResolvingResource {

    private final String sql;
    private final String keyColumn;
    private final String valueColumn;
    private String type;
    private final Properties dbProperties;

    public DataBaseResource(String sql, String keyColumn, String valueColumn, String type,Properties dbProperties) {
        this.sql = sql;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.type = type;
        this.dbProperties = dbProperties;
    }



    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return null;
    }


    public Properties getProperties() throws SQLException {
        Properties result4 = new Properties();
        try (Connection con = DriverManager.getConnection(this.dbProperties.getProperty("url"),dbProperties)) {
            ResultSet resultSet = con.prepareStatement(this.sql).executeQuery();
            while (Objects.nonNull(resultSet) && resultSet.next()) {
                result4.put(resultSet.getString(this.keyColumn), resultSet.getObject(this.valueColumn));
            }

        }
        return result4;
    }

}
