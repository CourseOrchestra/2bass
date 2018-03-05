package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.Connection;

public abstract class ApplyTest {

    Bass bass;

    @BeforeEach
    void beforeEach() throws Exception {
        this.bass = new Bass(getProperties());
    }

    @AfterEach
    void afterEach() throws Exception {
        this.bass.close();
        this.bass = null;
    }

    AppProperties getProperties() {
        AppProperties properties = new AppProperties();
        String scorePath1 = getClass().getResource("appTestScores/applyScore/s1").getPath();
        String scorePath2 = getClass().getResource("appTestScores/applyScore/s2").getPath();
        properties.setScorePath(scorePath1 + File.pathSeparator + scorePath2);
        return properties;
    }


    @Test
    void testApply() throws Exception {
        bass.updateDb();

        DBAdaptor dbAdaptor = bass.dbAdaptor;

        try (Connection conn = bass.connectionPool.get()) {
            assertTrue(dbAdaptor.tableExists(conn, "market", "customers"));
        }
    }
}
