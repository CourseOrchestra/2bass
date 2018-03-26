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

    OutputStreamDdlConsumer(OutputStream os) {
        this.os = os;
    }

    @Override
    public void consume(Connection conn, String sql) throws CelestaException {
        try {
            this.os.write((sql + ";\n").getBytes());
            this.os.flush();
        } catch (IOException e) {
            throw new CelestaException(e);
        }
    }
}
