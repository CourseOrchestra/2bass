package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;

public class H2BassTest extends BassTest {

    @AfterEach
     void afterEach() throws Exception {
        bass.getConnectionPool().get().createStatement().execute("SHUTDOWN");
        super.afterEach();
    }

    @Override
    AppProperties getProperties() {
        AppProperties properties = super.getProperties();
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");
        return properties;
    }
}
