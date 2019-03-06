package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.score.ParseException;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private Bass bass;
    private MockConsoleHelper ch;

    @BeforeEach
    void beforeEach(){
        ch = new MockConsoleHelper();
    }

    @AfterEach
    void afterEach() throws Exception {
        bass.getConnectionPool().get().createStatement().execute("SHUTDOWN");
        bass.close();
        bass = null;
        ch = null;
    }

    @Test
    void setupDBConnection() throws Exception {
        createBass("appTestScores/applyScore");
        bass.setupDBConnection();
        assertEquals(0, ch.activePhaseCount);
        assertEquals(2, ch.messages.size());
        assertTrue(ch.messages.get(1).contains("Connecting to"));
    }

    @Test
    void testUpdateEmptyDb() throws Exception {
        createBass("appTestScores/emptyScore");
        bass.updateDb();
        assertTrue(bass.getDbAdaptor().tableExists(bass.getConnectionPool().get(),
                Score.SYSTEM_SCHEMA_NAME, SchemaDataAccessor.TABLE_NAME));
        assertEquals(0, ch.activePhaseCount);
        assertEquals(3, ch.messages.size());
        assertTrue(ch.messages.get(2).contains("Updating"));
    }

    @Test
    void testExecuteNativeBefore() throws Exception {
        createBass("appTestScores/executeNativeScore");
        bass.updateDb();

        try (Connection conn = bass.getConnectionPool().get()) {
            assertAll(
                    () -> assertTrue(() -> tableExists(conn, "before", "t")),
                    () -> assertTrue(() -> tableExists(conn, "after", "t")),
                    () -> assertFalse(() -> tableExists(conn, "around", "t1")),
                    () -> assertTrue(() -> tableExists(conn, "around", "t2"))

            );
        }
    }

    private void createBass(String scoreResourcePath) throws CelestaException, ParseException {
        String scorePath = getClass().getResource(scoreResourcePath).getPath();
        AppProperties properties = new AppProperties();
        properties.setScorePath(scorePath);
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");

        bass = new Bass(properties, ch);
        assertEquals(0, ch.activePhaseCount);
        assertEquals("Parsing SQL scripts",
                ch.messages.get(0));
    }

    private boolean tableExists(Connection conn, String schemaName, String tableName) {
        return bass.getDbAdaptor().tableExists(conn, schemaName, tableName);
    }
}
