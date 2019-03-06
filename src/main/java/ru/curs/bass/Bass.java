package ru.curs.bass;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.ConnectionPool;
import ru.curs.celesta.ConnectionPoolConfiguration;
import ru.curs.celesta.CurrentScore;
import ru.curs.celesta.DBType;
import ru.curs.celesta.dbutils.DbUpdater;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.adaptors.configuration.DbAdaptorFactory;
import ru.curs.celesta.dbutils.adaptors.ddl.DdlConsumer;
import ru.curs.celesta.dbutils.adaptors.ddl.JdbcDdlConsumer;
import ru.curs.celesta.score.ParseException;
import ru.curs.celesta.score.discovery.ScoreByScorePathDiscovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public final class Bass implements AutoCloseable {
    private final ConsoleHelper consoleHelper;
    private final AppProperties properties;
    private DbUpdater<?> dbUpdater;
    private DBAdaptor dbAdaptor;
    private ConnectionPool connectionPool;
    private DdlConsumer ddlConsumer;
    private Score score;

    Bass(AppProperties properties, ConsoleHelper consoleHelper) throws CelestaException, ParseException {
        this.properties = properties;
        this.consoleHelper = consoleHelper;
        parseSQL();
    }

    void setupDBConnection() {
        //CONN POOL
        consoleHelper.phase(String.format("Connecting to %s", properties.getJdbcUrl()));
        ConnectionPoolConfiguration cpc = new ConnectionPoolConfiguration();
        cpc.setJdbcConnectionUrl(properties.getJdbcUrl());

        DBType dbType = DBType.resolveByJdbcUrl(properties.getJdbcUrl());

        cpc.setDriverClassName(dbType.getDriverClassName());
        cpc.setLogin(properties.getJdbcUserName());
        cpc.setPassword(properties.getJdbcPassword());
        this.connectionPool = ConnectionPool.create(cpc);

        //DBA
        DbAdaptorFactory daf = new DbAdaptorFactory()
                .setDbType(dbType)
                .setConnectionPool(connectionPool)
                .setH2ReferentialIntegrity(true);

        boolean updatingIsDisabled = false;
        if (properties.getCommand() == App.Command.PLAN) {
            updatingIsDisabled = true;
            try {
                final OutputStream os;
                if (properties.getFilePath() != null) {
                    File f = new File(properties.getFilePath());
                    if (!f.getParentFile().exists())
                        f.mkdirs();
                    os = new FileOutputStream(f);
                    this.ddlConsumer = new OutputStreamDdlConsumer(os);
                } else {
                    this.ddlConsumer = new ConsoleDdlConsumer(consoleHelper);
                }
            } catch (FileNotFoundException e) {
                throw new BassException(e);
            }
        } else
            this.ddlConsumer = new JdbcDdlConsumer();
        daf.setDdlConsumer(this.ddlConsumer);

        this.dbAdaptor = daf.createDbAdaptor();
        try (Connection conn = connectionPool.get()) {
            if (!dbAdaptor.isValidConnection(conn, 10))
                throw new CelestaException("Cannot connect to database.");
        } catch (SQLException e) {
            throw new BassException(e);
        }
        this.dbUpdater = new DbUpdaterImpl(connectionPool, score, true, dbAdaptor, updatingIsDisabled);
        consoleHelper.done();
    }

    private void parseSQL() throws ParseException {
        //SCORE
        consoleHelper.phase("Parsing SQL scripts");
        score = new Score.ScoreBuilder<>(Score.class)
                .scoreDiscovery(new ScoreByScorePathDiscovery(properties.getScorePath()))
                .build();
        CurrentScore.set(score);
        consoleHelper.done();
    }

    void initSystemSchema() {
        setupDBConnection();
        try {
            consoleHelper.phase("Updating system schema");
            dbUpdater.updateSystemSchema();
            consoleHelper.done();
        } catch (CelestaException e) {
            throw new BassException(e);
        }
    }

    void updateDb() {
        setupDBConnection();
        try {
            consoleHelper.phase("Updating");
            dbUpdater.updateDb();
            consoleHelper.done();
        } catch (CelestaException e) {
            throw new BassException(e);
        }
    }

    void outputDdlScript() {
        setupDBConnection();
        try {
            try (Connection conn = connectionPool.get()) {
                if (!dbAdaptor.tableExists(conn, score.getSysSchemaName(), SchemaDataAccessor.TABLE_NAME)) {
                    System.out.println("System schema is not initialized. Use \"bass init\" to do it.");
                    return;
                }
            }
            consoleHelper.phase("Outputting");
            dbUpdater.updateDb();
            consoleHelper.done();
        } catch (SQLException | CelestaException e) {
            throw new BassException(e);
        }
    }

    @Override
    public void close() {
        if (connectionPool != null)
            connectionPool.close();
    }

    DBAdaptor getDbAdaptor() {
        return dbAdaptor;
    }

    ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    DdlConsumer getDdlConsumer() {
        return ddlConsumer;
    }

    Score getScore() {
        return score;
    }
}
