package com.property.source.config.model;

import com.property.source.config.service.ValueDecoder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringValueResolver;

import java.util.Objects;

public class CustomPropertySource extends PropertySourcesPlaceholderConfigurer {

    private ValueDecoder valueDecoder;


    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess, StringValueResolver valueResolver) {
        super.doProcessProperties(beanFactoryToProcess,
                strVal -> convertPropertyValue(Objects.requireNonNull(valueResolver.resolveStringValue(strVal)))
        );
    }

    @Override
    protected String convertPropertyValue(String originalValue) {

        if(originalValue.startsWith("DEC(") && originalValue.endsWith(")")){
            if(Objects.isNull(valueDecoder))
                throw new RuntimeException("Value Decryptor bean is missing");
            return valueDecoder.decode(getStringBetween(originalValue));
        }
        return super.convertPropertyValue(originalValue);
    }


    private String getStringBetween(String originalValue){
        return originalValue.substring(originalValue.indexOf("(")+1,originalValue.lastIndexOf(")"));
    }

    public void setValueDecoder(ValueDecoder valueDecoder) {
        this.valueDecoder = valueDecoder;
    }
}
