package ru.curs.bass;

import info.macias.kaconf.Configurator;
import info.macias.kaconf.ConfiguratorBuilder;
import info.macias.kaconf.sources.JavaUtilPropertySource;
import org.fusesource.jansi.AnsiConsole;
import ru.curs.celesta.*;
import ru.curs.celesta.score.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import static org.fusesource.jansi.Ansi.ansi;


/**
 * Hello world!
 */
public class App {

    private static final String PROPERTIES_ENV_KEY = "BASS_PROPERTIES";

    private static final String HELP =
            ansi().a("Usage: bass <command> [properties file]\n")
                    .a("Available commands are:\n")
                    .bold().a("\tinit").reset().a("\t\t Init system schema\n")
                    .bold().a("\tapply").reset().a("\t\t Build or change database structure\n")
                    .bold().a("\tplan").reset().a("\t\t Generate and show DDL execution plan\n")
                    .a("Required setup properties are:\n")
                    .bold().a("\tscore.path").reset().a("\t\t Path to SQL scripts\n")
                    .bold().a("\tjdbc.url").reset().a("\t\t JDBC connection URL\n")
                    .bold().a("\tjdbc.username").reset().a("\t\t Database user name\n")
                    .bold().a("\tjdbc.password").reset().a("\t\t Database password\n")
                    .toString();

    private static final Map<String, Consumer<Bass>> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put(Command.INIT.toString(), Bass::initSystemSchema);
        COMMANDS.put(Command.IMPORT.toString(), Bass::toString); //TODO:
        COMMANDS.put(Command.PLAN.toString(), Bass::outputDdlScript);
        COMMANDS.put(Command.APPLY.toString(), Bass::updateDb);
        COMMANDS.put(Command.VALIDATE.toString(), bass -> {
        });
    }

    static ConsoleHelper consoleHelper = new ConsoleHelper(System.out);

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        try {
            consoleHelper.info("This is 2bass.");

            if (args.length == 0) {
                consoleHelper.error("No command was specified.");
                consoleHelper.info(HELP);
                return;
            }
            String cmd = args[0];
            Consumer<Bass> bassConsumer = COMMANDS.get(cmd);

            if (bassConsumer == null) {
                consoleHelper.error("Invalid command was specified.\n");
                consoleHelper.info(HELP);
            } else {
                String propertiesPath;
                if (args.length > 1) {
                    propertiesPath = args[1];
                } else {
                    propertiesPath = System.getenv(PROPERTIES_ENV_KEY);
                }
                if (propertiesPath == null) {
                    propertiesPath = "bass.properties";
                }
                File propertiesFile = new File(propertiesPath);
                if (!(propertiesFile.exists() && propertiesFile.canRead())) {
                    consoleHelper.error(String.format("Properties file %s does not exists or cannot be read.%n",
                            propertiesFile.getAbsolutePath()));
                    consoleHelper.info(HELP);
                    return;
                }
                AppProperties properties = readProperties(propertiesFile);
                properties.setCommand(Command.getByString(cmd));


                try {
                    if (properties.getCommand() == Command.VALIDATE) {
                        consoleHelper.phase("Parsing SQL scripts");
                        Bass.getScore(properties);
                        consoleHelper.done();
                    } else {
                        Bass bass = new Bass(properties, consoleHelper);
                        bassConsumer.accept(bass);
                        bass.close();
                    }
                } catch (ParseException | CelestaException | BassException e) {
                    consoleHelper.error(e.getMessage());
                    if (properties.isDebug())
                        e.printStackTrace();
                }
            }
        } finally {
            AnsiConsole.systemUninstall();
            if (consoleHelper.isError()) {
                consoleHelper.sysExit(1);
            }
        }
    }


    private static AppProperties readProperties(File propertiesFile) {

        try (FileInputStream fis = new FileInputStream(propertiesFile.getAbsolutePath())) {
            AppProperties properties = new AppProperties();

            Properties propsFromFile = new Properties();
            propsFromFile.load(fis);
            Configurator configurator = new ConfiguratorBuilder()
                    .addSource(new JavaUtilPropertySource(propsFromFile))
                    .build();

            configurator.configure(properties);
            return properties;
        } catch (FileNotFoundException e) {
            throw new BassException("There is no properties file on path " + propertiesFile.toString());
        } catch (IOException e) {
            throw new BassException(e);
        }

    }


    enum Command {
        INIT,
        IMPORT,
        PLAN,
        APPLY,
        VALIDATE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        static Command getByString(String str) {
            return Arrays.stream(values()).filter(
                    v -> v.toString().equals(str)
            ).findFirst().orElse(null);
        }
    }
}
