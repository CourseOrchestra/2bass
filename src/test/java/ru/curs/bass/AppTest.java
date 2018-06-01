package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.ConnectionPool;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.score.ParseException;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private Bass bass;
    private DBAdaptor dbAdaptor;
    private ConnectionPool cp;


    @AfterEach
    void afterEach() throws Exception {
        this.cp.get().createStatement().execute("SHUTDOWN");
        this.bass.close();

        this.bass = null;
        this.dbAdaptor = null;
        this.cp = null;
    }

    @Test
    void testUpdateEmptyDb() throws Exception {
        createBass("appTestScores/emptyScore");
        bass.updateDb();
        assertTrue(dbAdaptor.tableExists(cp.get(), Score.SYSTEM_SCHEMA_NAME, SchemaDataAccessor.TABLE_NAME));
    }

    @Test
    void testExecuteNativeBefore() throws Exception {
        createBass("appTestScores/executeNativeScore");
        bass.updateDb();

        Connection conn = cp.get();

        assertAll(
                () -> assertTrue(() -> tableExists(conn, "before", "t")),
                () -> assertTrue(() -> tableExists(conn, "after", "t")),
                () -> assertFalse(() -> tableExists(conn, "around", "t1")),
                () -> assertTrue(() -> tableExists(conn, "around", "t2"))

        );
    }

    private void createBass(String scoreResourcePath) throws CelestaException, ParseException {
        String scorePath = getClass().getResource(scoreResourcePath).getPath();
        AppProperties properties = new AppProperties();
        properties.setScorePath(scorePath);
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");

        MockConsoleHelper ch = new MockConsoleHelper();
        this.bass = new Bass(properties, ch);

        assertEquals(0, ch.activePhaseCount);
        assertEquals("Parsing SQL scripts",
                ch.messages.get(0));

        this.dbAdaptor = bass.dbAdaptor;
        this.cp = bass.connectionPool;
    }

    private boolean tableExists(Connection conn, String schemaName, String tableName) {
        return dbAdaptor.tableExists(conn, schemaName, tableName);
    }
}
