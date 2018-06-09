package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSqlBassTest extends BassTest {

    PostgreSQLContainer postgres;

    @BeforeEach
    void beforeEach() throws Exception {
        postgres = new PostgreSQLContainer();
        postgres.start();
        super.beforeEach();
    }


    @AfterEach
    void afterEach() throws Exception {
        super.afterEach();
        postgres.stop();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl(postgres.getJdbcUrl());
        properties.setJdbcUserName(postgres.getUsername());
        properties.setJdbcPassword(postgres.getPassword());
        return properties;
    }

}
