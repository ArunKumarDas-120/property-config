package com.property.source.config.factory;

import com.property.source.config.model.DataBaseResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.Properties;

public class DataBasePropertiesFactoryBean {

    @Nullable
    private Resource[] locations;

    public void setLocations(Resource... locations) {
        this.locations = locations;
    }

    public Properties getObject() throws Exception {
        Properties properties = new Properties();
        if (Objects.nonNull(locations)) {
            Resource[] var1 = this.locations;
            for (int var2 = 0; var2 < var1.length; var2++) {
                DataBaseResource res = (DataBaseResource) var1[var2];
                properties.putAll(res.getProperties());
            }
        }

        return properties;
    }


}
