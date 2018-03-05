package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MSSQLServerContainer;

public class MySqlApplyTest extends ApplyTest {

    MSSQLServerContainer mssql;

    @BeforeEach
    void beforeEach() throws Exception {
        mssql = new MSSQLServerContainer();
        mssql.start();
        super.beforeEach();
    }


    @AfterEach
    void afterEach() throws Exception {
        super.afterEach();
        mssql.stop();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl(mssql.getJdbcUrl());
        properties.setJdbcUserName(mssql.getUsername());
        properties.setJdbcPassword(mssql.getPassword());
        return properties;
    }
}
