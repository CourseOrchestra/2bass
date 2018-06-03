package ru.curs.bass;

import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleHelper {
    private int i = 1;
    private final PrintStream out;
    private boolean error = false;

    public ConsoleHelper(PrintStream out) {
        this.out = out;
    }

    public void phase(String description) {
        out.println(ansi().bold().a(i).a(". ").a(description).a("...").reset());
        i++;
    }

    public void done() {
        out.println(ansi().fgBrightGreen().a("   done.").reset());
        out.println();
    }

    public final void error(String message) {
        error = true;
        errMessage(message);
    }

    protected void errMessage(String message){
        out.println(ansi().fgBrightRed().a("   ERROR: ").a(message).reset());
    }

    public void info(String s) {
        out.println(s);
    }

    public boolean isError() {
        return error;
    }

    public void sysExit(int rc) {
        System.exit(rc);
    }
}

