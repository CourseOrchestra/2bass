package ru.curs.bass;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HighlighterTest {
    @Test
    void stringSplittedAndHighlighted() {
        String example = "table \"customers TABLE\" " +
                "( INT /* *comment* */ 2.2e14'text''text'--comment";
        SyntaxHighlighter h = new SyntaxHighlighter();
        Ansi result = ansi();
        h.highlightString(example, result);

        LexemHighlighter id = new IdentifierHighlighter();
        LexemHighlighter aq = new AnsiQuotedIdHighlighter();
        LexemHighlighter mlc = new MultiLineCommentHighlighter();
        LexemHighlighter dl = new DigitalLiteralHighlighter();
        LexemHighlighter sl = new StringLiteralHighlighter();
        LexemHighlighter lc = new LineCommentHighlighter();

        Ansi expected = ansi();
        id.colorize(expected, "table");
        expected.a(" ");
        aq.colorize(expected, "\"customers TABLE\"");
        expected.a(" ( ");
        id.colorize(expected, "INT");
        expected.a(" ");
        mlc.colorize(expected, "/* *comment* */");
        expected.a(" ");
        dl.colorize(expected, "2.2e14");
        sl.colorize(expected, "'text''text'");
        lc.colorize(expected, "--comment");

        assertEquals(expected.toString(), result.toString());
    }
}
