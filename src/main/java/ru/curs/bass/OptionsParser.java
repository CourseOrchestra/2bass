package ru.curs.bass;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Auxiliary class for parsing command line options.
 */
public final class OptionsParser {
    static final String PROPERTIES_FILE = "propertiesFile";
    private static final String[][] DESCR = {
            {"score.path", "path", "Path to SQL scripts"},
            {"jdbc.url", "url", "JDBC connection URL"},
            {"jdbc.username", "username", "Database user name"},
            {"jdbc.password", "password", "Database password"},
            {"outputFilePath", "path", "Path to write scripts to (for `plan` command)"},
            {PROPERTIES_FILE, "path",
                    "Properties file with options (options set in command line have higher priority)"}};

    private final Options options;
    private final ConsoleHelper consoleHelper;

    OptionsParser(ConsoleHelper consoleHelper) {
        this.consoleHelper = consoleHelper;
        options = new Options();

        for (String[] optionDescr : DESCR) {
            options.addOption(createLongOption(optionDescr[0], optionDescr[1], optionDescr[2]));
        }
        options.addOption(Option.builder().longOpt("debug").desc("Debug mode: show exception stack traces").build());
    }

    void help() {
        consoleHelper.info(
                ansi().a("Usage: bass <command> <options>").newline()
                        .newline()
                        .a("Available commands are:").newline()
                        .bold().a("\tvalidate").reset().a("\t Parse and validate SQL scripts").newline()
                        .bold().a("\tinit").reset().a("\t\t Init system schema").newline()
                        .bold().a("\tplan").reset().a("\t\t Generate and show DDL execution plan").newline()
                        .bold().a("\tapply").reset().a("\t\t Build or change database structure").newline().toString());
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(0);
        formatter.setLongOptSeparator("=");
        formatter.setSyntaxPrefix("");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, formatter.getWidth(), "Options are: ",
                null, options, formatter.getLeftPadding(), formatter.getDescPadding(),
                null, true);
        pw.flush();
        consoleHelper.info(sw.toString());
    }

    Properties getProperties(String[] args) throws BassException {
        Properties props = new Properties();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            for (Option option : cmd.getOptions()) {
                props.put(option.getLongOpt(), option.hasArg() ? option.getValue() : "true");
            }
            return props;
        } catch (ParseException e) {
            throw new BassException(e.getMessage());
        }
    }

    private static Option createLongOption(String name, String paramName, String descr) {
        return Option.builder()
                .valueSeparator()
                .hasArg()
                .longOpt(name)
                .argName(paramName)
                .desc(descr)
                .build();
    }
}
