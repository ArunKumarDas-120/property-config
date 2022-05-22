package com.property.source.config.model;

import java.util.ArrayList;
import java.util.List;

public class PropertySourceConfig {

    private final List<PropertySource>  config = new ArrayList<>();

    public List<PropertySource> getConfig() {
        return config;
    }


    public static class PropertySource {
        private String from;
        private String name;
        private String query;
        private String keyColumn;
        private String valueColumn;
        private String type;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getKeyColumn() {
            return keyColumn;
        }

        public void setKeyColumn(String keyColumn) {
            this.keyColumn = keyColumn;
        }

        public String getValueColumn() {
            return valueColumn;
        }

        public void setValueColumn(String valueColumn) {
            this.valueColumn = valueColumn;
        }

         public String getType() {
             return type;
         }

         public void setType(String type) {
             this.type = type;
         }
     }
}
