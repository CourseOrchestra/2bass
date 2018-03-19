package ru.curs.bass;

import ru.curs.celesta.*;
import ru.curs.celesta.dbutils.DbUpdater;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.adaptors.configuration.DbAdaptorFactory;
import ru.curs.celesta.dbutils.adaptors.ddl.DdlConsumer;
import ru.curs.celesta.dbutils.adaptors.ddl.JdbcDdlConsumer;
import ru.curs.celesta.score.ParseException;
import ru.curs.celesta.score.discovery.DefaultScoreDiscovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class Bass implements AutoCloseable {

    final DBAdaptor dbAdaptor;
    final DbUpdater dbUpdater;
    final ConnectionPool connectionPool;
    final DdlConsumer ddlConsumer;
    final Score score;

    Bass(AppProperties properties) throws CelestaException, ParseException {
        //SCORE
        System.out.printf("1. Parsing SQL scripts...");
        score = new Score.ScoreBuilder<>(Score.class)
                .path(properties.getScorePath())
                .scoreDiscovery(new DefaultScoreDiscovery())
                .build();
        CurrentScore.set(score);
        System.out.println("done.");

        //CONN POOL
        System.out.printf("2. Connecting to %s...", properties.getJdbcUrl());
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

        if (properties.getTask() == App.Task.PLAN) {
            try {
                final OutputStream os;
                if (properties.getFilePath() != null) {
                    File f = new File(properties.getFilePath());
                    if (!f.getParentFile().exists())
                        f.mkdirs();
                    os = new FileOutputStream(f);
                } else {
                    os = System.out;
                }

                this.ddlConsumer = new OutputStreamDdlConsumer(os);
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
        this.dbUpdater = new DbUpdaterImpl(connectionPool, score, true, this.dbAdaptor);
        System.out.println("done.");
    }

    void initSystemSchema() {
        try {
            System.out.printf("3. Updating system schema...");
            dbUpdater.updateSystemSchema();
            System.out.println("done.");
        } catch (CelestaException e) {
            throw new BassException(e);
        }
    }

    void updateDb() {
        try {
            System.out.printf("3. Updating...");
            dbUpdater.updateDb();
            System.out.println("done.");
        } catch (CelestaException e) {
            throw new BassException(e);
        }
    }

    void outputDdlScript() {
        try {
            try (Connection conn = connectionPool.get()) {
                if (!dbAdaptor.tableExists(conn, score.getSysSchemaName(), SchemaDataAccessor.TABLE_NAME)) {
                    System.out.println("System schema is not initialized. Use \"bass init\" to do it.");
                    return;
                }
            }
            System.out.println("3. Outputting...");
            dbUpdater.updateDb();
            System.out.printf("done.");
        } catch (SQLException | CelestaException e) {
            throw new BassException(e);
        }
    }

    @Override
    public void close() {
        connectionPool.close();
    }
}
