package ru.curs.bass;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.adaptors.ddl.DdlConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class OutputStreamDdlConsumer implements DdlConsumer {

    private final OutputStream os;
    private final List<String> allStatements = new ArrayList<>();

    OutputStreamDdlConsumer(OutputStream os) {
        this.os = os;
    }

    @Override
    public void consume(Connection conn, String sql) throws CelestaException {
        try {
            this.os.write((sql + ";\n").getBytes());
            this.os.flush();
            this.allStatements.add(sql);
        } catch (IOException e) {
            throw new CelestaException(e);
        }
    }

    public List<String> getAllStatements() {
        return this.allStatements;
    }
}
