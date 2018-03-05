package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;

public class H2ApplyTest extends ApplyTest {

    @AfterEach
     void afterEach() throws Exception {
        this.bass.connectionPool.get().createStatement().execute("SHUTDOWN");
        super.afterEach();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");
        return properties;
    }
}
