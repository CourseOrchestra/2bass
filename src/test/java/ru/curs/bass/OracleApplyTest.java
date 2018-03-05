package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.OracleContainer;

import java.util.Locale;


public class OracleApplyTest extends ApplyTest {

    static {
        Locale.setDefault(Locale.US);
    }

    OracleContainer oracle;

    @BeforeEach
    void beforeEach() throws Exception  {
        oracle = new OracleContainer();
        oracle.start();
        super.beforeEach();
    }


    @AfterEach
    void afterEach() throws Exception {
        super.afterEach();
        oracle.stop();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl(oracle.getJdbcUrl().replace("localhost", "0.0.0.0"));
        properties.setJdbcUserName(oracle.getUsername());
        properties.setJdbcPassword(oracle.getPassword());
        return properties;
    }
}
