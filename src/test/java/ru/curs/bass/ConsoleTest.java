package ru.curs.bass;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleTest {
    @Test
    void exitWithNoParams() {
        final StringBuilder sb = new StringBuilder();
        App.consoleHelper = new MockConsoleHelper(sb::append);
        assertEquals("1",
                assertThrows(
                        MockSysExitException.class,
                        () -> App.main(new String[]{})
                ).getMessage());
        assertTrue(sb.toString().contains("No command was specified"));
    }

    @Test
    void exitWithInvalidParams() {
        final StringBuilder sb = new StringBuilder();
        App.consoleHelper = new MockConsoleHelper(sb::append);
        assertEquals("1",
                assertThrows(
                        MockSysExitException.class,
                        () -> App.main(new String[]{"foo"})
                ).getMessage());
        assertTrue(sb.toString().contains("Invalid command was specified"));
    }

    @Test
    void validCommandInvalidParams() {
        final StringBuilder sb = new StringBuilder();
        App.consoleHelper = new MockConsoleHelper(sb::append);
        assertEquals("1",
                assertThrows(MockSysExitException.class,
                        () -> App.main(new String[]{"validate", "--foo"})
                ).getMessage());
        System.out.println(sb.toString());
        assertTrue(sb.toString().contains("Unrecognized option: --foo"));
    }

    @Test
    void nonExistentPropertiesFile() {
        final StringBuilder sb = new StringBuilder();
        App.consoleHelper = new MockConsoleHelper(sb::append);
        assertEquals("1",
                assertThrows(MockSysExitException.class,
                        () -> App.main(new String[]{"validate", "--propertiesFile=NOTEXISTS"})
                ).getMessage());
        assertTrue(sb.toString().contains("does not exists or cannot be read"));
    }

    @Test
    void validateCommandInvalidScore() throws IOException {
        final StringBuilder sb = new StringBuilder();
        App.consoleHelper = new MockConsoleHelper(sb::append);
        Properties p = new Properties();
        String scoreResourcePath = "appTestScores/invalidScore";
        p.setProperty("score.path", getClass().getResource(scoreResourcePath).getPath());
        File f = File.createTempFile("2basstest", "tmp");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(f),
                        StandardCharsets.UTF_8))) {
            p.store(pw, null);
            assertEquals("1",
                    assertThrows(MockSysExitException.class,
                            () -> App.main(new String[]{"validate", "--propertiesFile=" + f.toString()})
                    ).getMessage());
            assertTrue(sb.toString().contains("Error parsing"));
        } finally {
            f.delete();
        }
    }

    @Test
    void validateCommandValidScore() throws IOException {
        MockConsoleHelper consoleHelper = new MockConsoleHelper();
        App.consoleHelper = consoleHelper;
        Properties p = new Properties();
        String scoreResourcePath = "appTestScores/applyScore";
        p.setProperty("score.path", getClass().getResource(scoreResourcePath).getPath());
        File f = File.createTempFile("2basstest", "tmp");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(f),
                        StandardCharsets.UTF_8))) {
            p.store(pw, null);
            App.main(new String[]{"validate", "--propertiesFile=" + f.toString()});
            assertTrue(consoleHelper.messages.get(0).contains("Parsing SQL scripts"));
            //Only 1 message: 'parsing'.
            assertEquals(1, consoleHelper.messages.size());
        } finally {
            f.delete();
        }
    }

    @Test
    void applyCommandValidScore() throws IOException {
        MockConsoleHelper consoleHelper = new MockConsoleHelper();
        App.consoleHelper = consoleHelper;
        Properties p = new Properties();
        String scoreResourcePath = "appTestScores/applyScore";
        p.setProperty("score.path", getClass().getResource(scoreResourcePath).getPath());
        p.setProperty("jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        File f = File.createTempFile("2basstest", "tmp");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(f),
                        StandardCharsets.UTF_8))) {
            p.store(pw, null);
            App.main(new String[]{"apply", "--propertiesFile=" + f.toString()});
            assertTrue(consoleHelper.messages.get(0).contains("Parsing SQL scripts"));
            //3 messages: 'parsing', 'connecting' and 'updating'.
            assertEquals(3, consoleHelper.messages.size());
        } finally {
            f.delete();
        }
    }
}
