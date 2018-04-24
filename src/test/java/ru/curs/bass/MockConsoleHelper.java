package ru.curs.bass;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class MockConsoleHelper extends ConsoleHelper {

    List<String> messages = new ArrayList<>();
    int activePhaseCount = 0;

    MockConsoleHelper() {
        super(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //silently swallow anything
            }
        }));
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
    public void error(String message){
        fail(message);
    }
}
