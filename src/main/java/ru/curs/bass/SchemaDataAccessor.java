package ru.curs.bass;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.CsqlBasicDataAccessor;
import ru.curs.celesta.dbutils.query.FromClause;
import ru.curs.celesta.dbutils.stmt.MaskedStatementHolder;
import ru.curs.celesta.dbutils.stmt.PreparedStatementHolderFactory;
import ru.curs.celesta.dbutils.stmt.PreparedStmtHolder;
import ru.curs.celesta.dbutils.term.AlwaysTrue;
import ru.curs.celesta.dbutils.term.FromTerm;
import ru.curs.celesta.score.*;
import ru.curs.celesta.syscursors.ISchemaCursor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

public class SchemaDataAccessor extends CsqlBasicDataAccessor<CallContext> implements ISchemaCursor {

    public static final String TABLE_NAME = "schemas";

    private String id;
    private String version;
    private int length;
    private String checksum;
    private int state;
    private Date lastmodified;
    private String message;

    private final Table meta;
    private final boolean updatingIsDisabled;

    private final PreparedStmtHolder get;
    private final MaskedStatementHolder insert;

    private boolean[] updateMask = new boolean[currentValues().length];
    private boolean[] nullUpdateMask = new boolean[currentValues().length];
    private final PreparedStmtHolder update;

    private ResultSet cursor = null;

    private final PreparedStmtHolder findSet;


    public SchemaDataAccessor(CallContext context, boolean updatingIsDisabled) throws CelestaException {
        super(context);
        this.updatingIsDisabled = updatingIsDisabled;

        try {
            this.meta = context.getScore()
                    .getGrain(Score.SYSTEM_SCHEMA_NAME)
                    .getElement(TABLE_NAME, Table.class);

            this.get = PreparedStatementHolderFactory.createGetHolder(meta(), db(), conn());
            this.insert = PreparedStatementHolderFactory.createInsertHolder(meta(), db(), conn());
            this.update = PreparedStatementHolderFactory.createUpdateHolder(
                    meta(), db(), conn(), () -> updateMask, () -> nullUpdateMask
            );
            this.findSet = PreparedStatementHolderFactory.createFindSetHolder(
                    db(),
                    conn(),
                    () -> {
                        FromClause result = new FromClause();
                        DataGrainElement ge = meta();

                        result.setGe(ge);
                        result.setExpression(db().tableString(ge.getGrain().getName(), ge.getName()));

                        return result;
                    },
                    () -> new FromTerm(Collections.emptyList()),
                    () -> AlwaysTrue.TRUE,
                    () -> "\"id\"",
                    () -> 0L,
                    () -> 0L,
                    () -> Collections.emptySet()
            );
        } catch (ParseException e) {
            throw new CelestaException(e);
        }

        context.addDataAccessor(this);
    }

    @Override
    public void clear() throws CelestaException {
        //TODO:!!! Эта абстракция не нужна
    }

    @Override
    protected void closeInternal() {
        //TODL!!! Эта абстакция может быть не нужна
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Integer getLength() {
        return length;
    }

    @Override
    public void setLength(Integer length) {
        this.length = length;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    @Override
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public Integer getState() {
        return state;
    }

    @Override
    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public Date getLastmodified() {
        return lastmodified;
    }

    @Override
    public void setLastmodified(Date lastmodified) {
        this.lastmodified = lastmodified;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public void update() throws CelestaException {
        if (updatingIsDisabled)
            return;
        try (PreparedStatement stmt = update.getStatement(currentValues(), 0)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new CelestaException(e);
        }
    }

    @Override
    public void get(Object... values) throws CelestaException {
        try (PreparedStatement stmt = get.getStatement(values, 0);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                parseResult(rs);
            } else {
                String sb = Arrays.stream(values).map(Objects::toString).collect(Collectors.joining(", "));
                if (!updatingIsDisabled)
                    throw new CelestaException("There is no %s (%s).", meta().getName(), sb);
            }
        } catch (SQLException e) {
            throw new CelestaException(e);
        }
    }

    @Override
    public void init() {
        //TODO:!!! Эта абстракция не нужна
    }


    @Override
    public boolean nextInSet() throws CelestaException {

        boolean result;
        try {
            if (cursor == null)
                result = findSet();
            else {
                result = cursor.next();
            }
            if (result) {
                parseResult(cursor);
            } else {
                cursor.close();
                cursor = null;
            }
        } catch (SQLException e) {
            result = false;
        }
        return result;
    }

    private boolean findSet() throws CelestaException {
        PreparedStatement ps = findSet.getStatement(currentValues(), 0);
        boolean result;
        try {
            if (cursor != null)
                cursor.close();
            cursor = ps.executeQuery();
            result = cursor.next();
            if (result) {
                parseResult(cursor);
            }
        } catch (SQLException e) {
            throw new CelestaException(e.getMessage());
        }
        return result;
    }

    private void parseResult(ResultSet rs) throws SQLException {
        id = rs.getString("id");
        version = rs.getString("version");
        length = rs.getInt("length");
        checksum = rs.getString("checksum");
        state = rs.getInt("state");
        lastmodified = rs.getTimestamp("lastmodified");
        message = rs.getString("message");
    }

    @Override
    public void insert() throws CelestaException {
        if (updatingIsDisabled)
            return;
        try (PreparedStatement stmt = insert.getStatement(currentValues(), 0)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new CelestaException(e);
        }
    }

    @Override
    public final Table meta() throws CelestaException {
        return meta;
    }

    private Object[] currentValues() {
        Object[] result = {id != null ? id.replace("\"", "") : null, version, length, checksum, state, lastmodified,
                message};
        return result;
    }

}
