package ru.curs.bass;

import org.junit.jupiter.api.Test;
import ru.curs.celesta.ConnectionPool;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit test for simple App.
 */
public class AppTest {


    @Test
    public void testUpdateEmptyDb() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setScorePath("score");
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");

        Bass bass = new Bass(properties);
        bass.updateDb();

        DBAdaptor dbAdaptor = bass.dbAdaptor;
        ConnectionPool cp = bass.connectionPool;

        assertTrue(dbAdaptor.tableExists(cp.get(), Score.SYSTEM_SCHEMA_NAME, SchemaDataAccessor.TABLE_NAME));
    }
}
