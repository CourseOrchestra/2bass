package ru.curs.bass;

import org.junit.jupiter.api.Assertions;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class MockConsoleHelper extends ConsoleHelper {

    List<String> messages = new ArrayList<>();
    int activePhaseCount = 0;
    final Consumer<String> errConsumer;

    MockConsoleHelper() {
        this(Assertions::fail);
    }

    MockConsoleHelper(Consumer<String> c) {
        super(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //silently swallow anything
            }
        }));
        errConsumer = c;
    }

    @Override
    public void phase(String description) {
        messages.add(description);
        activePhaseCount++;
    }

    @Override
    public void done() {
        activePhaseCount--;
    }

    @Override
    public void error(String message) {
        error();
        errConsumer.accept(message);
    }

    @Override
    public void sysExit(int rc) {
        throw new MockSysExitException(rc);
    }
}

class MockSysExitException extends RuntimeException {
    MockSysExitException(int i) {
        super(Integer.toString(i));
    }
}