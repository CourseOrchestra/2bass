package ru.curs.bass;

import ru.curs.celesta.*;
import ru.curs.celesta.dbutils.DbUpdater;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.adaptors.configuration.DbAdaptorBuilder;
import ru.curs.celesta.score.ParseException;
import ru.curs.celesta.score.discovery.DefaultScoreDiscovery;

import java.sql.Connection;
import java.sql.SQLException;

public class Bass implements AutoCloseable {

    final DBAdaptor dbAdaptor;
    final DbUpdater dbUpdater;
    final ConnectionPool connectionPool;

    Bass(AppProperties properties) throws CelestaException, ParseException {
        //SCORE
        System.out.printf("1. Parsing SQL scripts...");
        Score score = new Score.ScoreBuilder<>(Score.class)
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
        DbAdaptorBuilder dac = new DbAdaptorBuilder()
                .setDbType(dbType)
                .setConnectionPool(connectionPool)
                .setH2ReferentialIntegrity(true);

        this.dbAdaptor = dac.createDbAdaptor();
        try (Connection conn = connectionPool.get()) {
            if (!dbAdaptor.isValidConnection(conn, 10))
                throw new CelestaException("Cannot connect to database.");
        } catch (SQLException e) {
            throw new CelestaException(e);
        }
        this.dbUpdater = new DbUpdaterImpl(connectionPool, score, true, this.dbAdaptor);
        System.out.println("done.");
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

    @Override
    public void close() {
        connectionPool.close();
    }
}
