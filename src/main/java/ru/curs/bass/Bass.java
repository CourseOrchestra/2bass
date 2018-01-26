package ru.curs.bass;

import ru.curs.celesta.*;
import ru.curs.celesta.dbutils.DbUpdater;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.adaptors.configuration.DbAdaptorBuilder;
import ru.curs.celesta.score.discovery.DefaultScoreDiscovery;

public class Bass implements AutoCloseable {

    final DBAdaptor dbAdaptor;
    final DbUpdater dbUpdater;
    final ConnectionPool connectionPool;

    Bass(AppProperties properties) throws CelestaException {
        //SCORE
        Score score = new Score.ScoreBuilder<>(Score.class)
                .path(properties.getScorePath())
                .scoreDiscovery(new DefaultScoreDiscovery())
                .build();
        CurrentScore.set(score);

        //CONN POOL
        ConnectionPoolConfiguration cpc = new ConnectionPoolConfiguration();
        cpc.setJdbcConnectionUrl(properties.getJdbcUrl());
        cpc.setDriverClassName(DBType.H2.getDriverClassName());
        cpc.setLogin(properties.getJdbcUserName());
        cpc.setPassword(properties.getJdbcPassword());
        this.connectionPool = ConnectionPool.create(cpc);

        //DBA
        DbAdaptorBuilder dac = new DbAdaptorBuilder()
                .setDbType(DBType.H2)
                .setConnectionPool(connectionPool)
                .setH2ReferentialIntegrity(true);

        this.dbAdaptor = dac.createDbAdaptor();
        this.dbUpdater = new DbUpdaterImpl(connectionPool, score, true, this.dbAdaptor);
    }

    void updateDb() {
        try {
            dbUpdater.updateDb();
        } catch (CelestaException e) {
            throw new BassException(e);
        }
    }

    @Override
    public void close() {
        connectionPool.close();
    }

}
