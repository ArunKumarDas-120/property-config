package com.test;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;



public class TestProperties extends TestConfig{
    @Value("${data.test}")
    private String valueFromProperty;
    @Value("${data.test1}")
    private String valueFromYml;
    @Value("${prop.db.key}")
    private String valueFromDB;


    @Test
    public void test(){
       Assert.assertEquals("value from properties",valueFromProperty);
        Assert.assertEquals("value from yml",valueFromYml);
        Assert.assertEquals("from db",valueFromDB);
    }

}
