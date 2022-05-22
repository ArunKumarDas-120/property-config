package com.property.source.config.configurer;

import com.property.source.config.annotation.EnableCustomPropertySource;
import com.property.source.config.model.CustomPropertySource;
import com.property.source.config.model.PropertySourceConfig;
import com.property.source.config.model.DataBaseResource;
import com.property.source.config.factory.DataBasePropertiesFactoryBean;
import com.property.source.config.service.ValueDecoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;


public class CustomPropertySourceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private Environment environment;
    private BeanFactory beanFactory;
    private final YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
    private final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
    private final DataBasePropertiesFactoryBean dataBasePropertiesFactoryBean = new DataBasePropertiesFactoryBean();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = new AnnotationAttributes(
                importingClassMetadata.getAnnotationAttributes(EnableCustomPropertySource.class.getCanonicalName()));
        BindResult<PropertySourceConfig> result = Binder.get(this.environment).bind("property.source", PropertySourceConfig.class);
        if (result.isBound()) {
            BindResult<Properties> dataBasePropBinding = Binder.get(environment).bind("spring.datasource", Properties.class);
            Properties dbProp = new Properties();
            dataBasePropBinding.ifBound(x -> {
                dbProp.putAll(x);
                dbProp.put("user", x.getProperty("username"));
            });
            try {
                List<Resource> propList = new ArrayList<>();
                List<Resource> ymlPropList = new ArrayList<>();
                List<Resource> dbList = new ArrayList<>();

                result.get().getConfig().forEach(s -> {
                    this.addPropertyResources(s, propList, ymlPropList, dbList, dbProp);
                });

                propertiesFactoryBean.setLocations(propList.toArray(new Resource[]{}));
                propertiesFactoryBean.afterPropertiesSet();
                yamlPropertiesFactoryBean.setResources(ymlPropList.toArray(new Resource[]{}));
                dataBasePropertiesFactoryBean.setLocations(dbList.toArray(new Resource[]{}));
                Properties props = new Properties();
                props.putAll(yamlPropertiesFactoryBean.getObject());
                props.putAll(propertiesFactoryBean.getObject());
                props.putAll(dataBasePropertiesFactoryBean.getObject());
                registry.registerBeanDefinition("customPropertySource", BeanDefinitionBuilder.
                        genericBeanDefinition(CustomPropertySource.class, () -> {
                            CustomPropertySource customPropertySource = new CustomPropertySource();
                            customPropertySource.setProperties(props);
                            customPropertySource.setIgnoreResourceNotFound(Boolean.TRUE);
                            customPropertySource.setValueDecoder(getBean(ValueDecoder.class));
                            return customPropertySource;
                        }).setScope("singleton").getBeanDefinition());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private <T> T getBean(Class<T> clazz) {
        T bean = null;
        try {
            bean = this.beanFactory.getBean(clazz);
        } catch (Exception e) {

        }
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void addPropertyResources(PropertySourceConfig.PropertySource propertySource,
                                      List<Resource> propList,
                                      List<Resource> ymlPropList
            , List<Resource> dbList, Properties dbProp) {
        Resource resource = null;
        if (propertySource.getFrom().equalsIgnoreCase("Classpath"))
            resource = new ClassPathResource(propertySource.getName().concat(".").concat(propertySource.getType()));
        else if (propertySource.getFrom().equalsIgnoreCase("File"))
            resource = new FileSystemResource(propertySource.getName().concat(".").concat(propertySource.getType()));
        else if (propertySource.getFrom().equalsIgnoreCase("DataBase"))
            resource = new DataBaseResource(propertySource.getQuery(), propertySource.getKeyColumn(),
                    propertySource.getValueColumn(), propertySource.getType(), dbProp);
        Optional.ofNullable(resource).ifPresent(r -> {
            if (propertySource.getFrom().equalsIgnoreCase("Classpath")
                    || propertySource.getFrom().equalsIgnoreCase("File")) {
                if (propertySource.getType().equalsIgnoreCase("properties"))
                    propList.add(r);
                else if (propertySource.getType().equalsIgnoreCase("yml"))
                    ymlPropList.add(r);
            } else if (propertySource.getFrom().equalsIgnoreCase("DataBase")) {
                dbList.add(r);
            }
        });
    }
}
