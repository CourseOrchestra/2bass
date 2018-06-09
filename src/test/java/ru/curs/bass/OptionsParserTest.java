package ru.curs.bass;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionsParserTest {
    private final MockConsoleHelper ch = new MockConsoleHelper();
    private final OptionsParser op = new OptionsParser(ch);

    @Test
    void mainOptionsAreParsed() {
        Properties p = op.getProperties(new String[]{"--jdbc.url=aa", "--debug"});
        assertEquals("aa", p.getProperty("jdbc.url"));
        assertEquals("true", p.getProperty("debug"));
        assertEquals(2, p.size());
    }

    @Test
    void missingOptionsParam() {
        assertEquals("Missing argument for option: jdbc.url",
                assertThrows(BassException.class,
                        () -> op.getProperties(
                                new String[]{"--jdbc.url", "--jdbc.password=123"})).getMessage());
    }

    @Test
    void invalidOption() {
        assertEquals("Unrecognized option: --fff",
                assertThrows(BassException.class,
                        () -> op.getProperties(
                                new String[]{"--jdbc.password=123", "--fff"})).getMessage());
    }

    @Test
    void produceHelp() {
        op.help();
        assertEquals(0, ch.activePhaseCount);
    }
}
