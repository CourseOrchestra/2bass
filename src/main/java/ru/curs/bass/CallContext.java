package ru.curs.bass;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.ICallContext;
import ru.curs.celesta.dbutils.CsqlBasicDataAccessor;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CallContext implements ICallContext {

    private final  DBAdaptor dbAdaptor;
    private final Connection conn;
    private final Score score;
    private final Set<SchemaDataAccessor> dataAccessors = new HashSet<>();
    private boolean closed;

    public CallContext(DBAdaptor dbAdaptor, Connection conn, Score score) {
        this.dbAdaptor = dbAdaptor;
        this.conn = conn;
        this.score = score;
    }


    @Override
    public Connection getConn() {
        return conn;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public DBAdaptor getDbAdaptor() {
        return dbAdaptor;
    }

    @Override
    public Score getScore() {
        return this.score;
    }

    @Override
    public void close() throws CelestaException {
        try {
            for (Iterator<SchemaDataAccessor> it = dataAccessors.iterator(); it.hasNext();) {
                CsqlBasicDataAccessor accessor = it.next();
                accessor.close();
            }

            conn.close();
            closed = true;
        } catch (Exception e) {
            throw new CelestaException("Can't close callContext", e);
        }
    }

    public void addDataAccessor(SchemaDataAccessor dataAccessor) {
        dataAccessors.add(dataAccessor);
    }
}
