package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class FirebirdBassTest extends BassTest {
    AdvancedFireBirdContainer firebird;

    @BeforeEach
    void beforeEach() throws Exception {
        firebird = new AdvancedFireBirdContainer();
        firebird.start();
        super.beforeEach();
    }


    @AfterEach
    void afterEach() throws Exception {
        super.afterEach();
        firebird.stop();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl(firebird.getJdbcUrl());
        properties.setJdbcUserName(firebird.getUsername());
        properties.setJdbcPassword(firebird.getPassword());
        return properties;
    }
}
