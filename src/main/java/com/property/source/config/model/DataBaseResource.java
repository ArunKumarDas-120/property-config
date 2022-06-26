package com.property.source.config.model;

import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DataBaseResource extends AbstractFileResolvingResource {

    private final String sql;
    private final String keyColumn;
    private final String valueColumn;
    private final String type;
    private final Properties dbProperties;


    public DataBaseResource(String sql, String keyColumn, String valueColumn, String type, Properties dbProperties) {
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
        Properties properties = new Properties();
        try (Connection con = DriverManager.getConnection(this.dbProperties.getProperty("url"), dbProperties)) {
            ResultSet resultSet = con.prepareStatement(this.sql).executeQuery();
            while (Objects.nonNull(resultSet) && resultSet.next()) {
                if ("yml".equalsIgnoreCase(this.type)) {
                    Map<String, Object> result = new LinkedHashMap<>();
                    this.buildFlattenedMap(result, this.parseYml(resultSet.getBlob(this.valueColumn)), null);
                    properties.putAll(result);
                } else
                    properties.put(resultSet.getString(this.keyColumn), resultSet.getObject(this.valueColumn));
            }

        }
        return properties;
    }

    private Map<String, Object> parseYml(Blob blob) throws SQLException {
        Yaml yml = new Yaml();
        return yml.load(new String(blob.getBytes(1, (int) blob.length())));
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }

            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    Iterator var7 = collection.iterator();

                    while (var7.hasNext()) {
                        Object object = var7.next();
                        buildFlattenedMap(result, Collections.singletonMap("[" + count++ + "]", object), key);
                    }
                }
            } else {
                result.put(key, value != null ? value : "");
            }

        });
    }

}
