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

import java.io.IOException;
import java.util.*;


public class CustomPropertySourceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private Environment environment;
    private BeanFactory beanFactory;

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
                List<Resource> proppertyResorceList = new ArrayList<>();
                result.get().getConfig().forEach(s -> {
                    this.addPropertyResources(s, proppertyResorceList, dbProp);
                });
                PropertiesFactory propertiesFactory = new PropertiesFactory(proppertyResorceList);
                Properties props = new Properties();
                propertiesFactory.populateProperties(props);
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
                                      List<Resource> proppertyResorceList, Properties dbProp) {
        Resource resource = null;
        if (propertySource.getFrom().equalsIgnoreCase("Classpath"))
            resource = new ClassPathResource(propertySource.getName().concat(".").concat(propertySource.getType()));
        else if (propertySource.getFrom().equalsIgnoreCase("File"))
            resource = new FileSystemResource(propertySource.getName().concat(".").concat(propertySource.getType()));
        else if (propertySource.getFrom().equalsIgnoreCase("DataBase"))
            resource = new DataBaseResource(propertySource.getQuery(), propertySource.getKeyColumn(),
                    propertySource.getValueColumn(), propertySource.getType(), dbProp);
        Optional.ofNullable(resource).ifPresent(r -> {
            proppertyResorceList.add(r);
        });
    }

    private class PropertiesFactory{
        final YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        final DataBasePropertiesFactoryBean dataBasePropertiesFactoryBean = new DataBasePropertiesFactoryBean();
        final List<Resource> propertySources = new ArrayList<>();
        PropertiesFactory(List<Resource> ...resourceList){
            Arrays.stream(resourceList).forEach(x-> propertySources.addAll(x));
        }

        private void populateProperties(Properties properties) throws Exception {
            for (Resource x : propertySources){
                if(x instanceof ClassPathResource || x instanceof FileSystemResource){
                    String name = x.getFilename();
                    if(name.endsWith(".properties")){
                        propertiesFactoryBean.setLocation(x);
                        propertiesFactoryBean.afterPropertiesSet();
                        properties.putAll(propertiesFactoryBean.getObject());
                    }else if(name.endsWith(".yml") || name.endsWith("YML")){
                        yamlPropertiesFactoryBean.setResources(x);
                        properties.putAll(yamlPropertiesFactoryBean.getObject());
                    }
                }else if(x instanceof DataBaseResource){
                    dataBasePropertiesFactoryBean.setLocations(x);
                    properties.putAll(dataBasePropertiesFactoryBean.getObject());
                }
            }
        }
    }
}
