package ru.curs.bass;

import org.fusesource.jansi.Ansi;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.adaptors.ddl.DdlConsumer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleDdlConsumer implements DdlConsumer {
    private final SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();
    private final ConsoleHelper ch;
    private final List<String> allStatements = new ArrayList<>();

    public ConsoleDdlConsumer(ConsoleHelper ch) {
        this.ch = ch;
    }

    @Override
    public void consume(Connection connection, String sql) throws CelestaException {
        this.allStatements.add(sql);
        Ansi ansi = ansi();
        syntaxHighlighter.highlightString(sql, ansi);
        ch.info(ansi.toString());
    }

    public List<String> getAllStatements() {
        return this.allStatements;
    }
}
