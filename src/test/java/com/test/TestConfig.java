package com.test;

import com.property.source.config.annotation.EnableCustomPropertySource;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@EnableCustomPropertySource
public class TestConfig  {


    @BeforeClass
    public static void startH2() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        RunScript.execute(conn,new InputStreamReader(TestConfig.class.getResourceAsStream("/db/tables.sql")));
        RunScript.execute(conn,new InputStreamReader(TestConfig.class.getResourceAsStream("/db/insert.sql")));
    }
}
