package ru.curs.bass;

import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    private LexemHighlighter[] hl = new LexemHighlighter[]{
            new IdentifierHighlighter(),
            new LineCommentHighlighter(),
            new MultiLineCommentHighlighter(),
            new DigitalLiteralHighlighter(),
            new StringLiteralHighlighter(),
            new AnsiQuotedIdHighlighter(),
    };

    public void highlightString(String input, Ansi output) {
        int pos = 0;
        while (pos < input.length()) {
            int delta = 0;
            lexemsearch:
            for (LexemHighlighter h : hl) {
                delta = h.consume(input, pos, output);
                if (delta > 0) {
                    pos += delta;
                    break lexemsearch;
                }
            }
            if (delta == 0) {
                //fallback to default
                output.a(input.charAt(pos));
                pos++;
            }
        }


    }
}

abstract class LexemHighlighter {
    int consume(String input, int pos, Ansi output) {
        Pattern p = Pattern.compile(getPattern());
        Matcher m = p.matcher(input.substring(pos));
        if (m.lookingAt()) {
            colorize(output, m.group());
            return m.group().length();
        } else {
            return 0;
        }
    }

    abstract void colorize(Ansi output, String group);

    abstract String getPattern();
}

class IdentifierHighlighter extends LexemHighlighter {
    private HashSet<String> keywords = new HashSet<>(Arrays.asList(
            "ACTION", "ADD", "ALTER", "AND",
            "AS", "BETWEEN", "BY", "CASCADE",
            "CONSTRAINT", "COUNT", "CREATE", "DEFAULT",
            "DELETE", "DISTINCT", "EXEC", "EXECUTE",
            "FALSE", "FOREIGN", "FROM", "FULL",
            "GETDATE", "GROUP", "IDENTITY", "IN",
            "INNER", "IS", "JOIN", "KEY",
            "LEFT", "LIKE", "MAX", "MIN",
            "NEXTVAL", "NO", "NOT", "NULL",
            "ON", "OR", "PRIMARY", "REFERENCES",
            "RIGHT", "SELECT", "SET", "SUM",
            "TEXT", "TRUE", "UPDATE", "WITH",
            "WHERE", "TABLE", "FUNCTION", "INDEX",
            "MATERIALIZED", "SEQUENCE", "VIEW",
            "INT", "VARCHAR", "REAL", "DATETIME", "BIT", "TEXT",
            "INTEGER", "DOUBLE",
            "START", "MINVALUE", "MAXVALUE", "INCREMENT",
            "TRIGGER", "COMMIT", "DROP", "IF", "THEN", "EXISTS",
            "BEGIN", "END", "RETURN", "LOCK", "ONLY",
            "COLUMN", "INSERT", "INTO", "VALUES", "DECLARE",
            "PROCEDURE", "BEFORE", "AFTER", "INSTEAD", "OF"
    ));


    @Override
    void colorize(Ansi output, String group) {
        if (keywords.contains(group.toUpperCase())) {
            output.bold().fgYellow().a(group).reset();
        } else {
            output.fgYellow().a(group).reset();
        }
    }

    @Override
    String getPattern() {
        return "[A-Za-z_][A-Za-z_0-9]*";
    }
}

class AnsiQuotedIdHighlighter extends LexemHighlighter {

    @Override
    void colorize(Ansi output, String group) {
        output.fgYellow().a(group).reset();
    }

    @Override
    String getPattern() {
        return "\"[^\"]*\"";
    }
}

class LineCommentHighlighter extends LexemHighlighter {

    @Override
    void colorize(Ansi output, String group) {
        output.fgGreen().a(group).reset();
    }

    @Override
    String getPattern() {
        return "--[^\r\n]*";
    }
}

class MultiLineCommentHighlighter extends LexemHighlighter {

    @Override
    void colorize(Ansi output, String group) {
        output.fgGreen().a(group).reset();
    }

    @Override
    String getPattern() {
        return "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
    }
}

class DigitalLiteralHighlighter extends LexemHighlighter {
    @Override
    void colorize(Ansi output, String group) {
        output.fgRed().a(group).reset();
    }

    String getPattern() {
        return "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
    }
}

class StringLiteralHighlighter extends LexemHighlighter {
    @Override
    void colorize(Ansi output, String group) {
        output.fgRed().a(group).reset();
    }

    String getPattern() {
        return "'[^']*'('([^'])*')*";
    }
}