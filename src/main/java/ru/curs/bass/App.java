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

    private static final Map<String, Consumer<Bass>> TASKS = new HashMap<>();

    static {
        TASKS.put(Task.INIT.toString(), Bass::initSystemSchema);
        TASKS.put(Task.IMPORT.toString(), Bass::toString); //TODO:
        TASKS.put(Task.PLAN.toString(), Bass::outputDdlScript);
        TASKS.put(Task.APPLY.toString(), Bass::updateDb);
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        ConsoleHelper ch = new ConsoleHelper(System.out);
        try {
            ch.info("This is 2bass.");

            if (args.length == 0) {
                ch.error("No command was specified.");
                ch.info(HELP);
                return;
            }
            String task = args[0];
            Consumer<Bass> bassConsumer = TASKS.get(task);

            if (bassConsumer == null) {
                ch.error("Invalid command was specified.\n");
                ch.info(HELP);
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
                    ch.error(String.format("Properties file %s does not exists or cannot be read.%n",
                            propertiesFile.getAbsolutePath()));
                    ch.info(HELP);
                    return;
                }
                AppProperties properties = readProperties(propertiesFile);
                properties.setTask(Task.getByString(task));

                try {
                    Bass bass = new Bass(properties, ch);
                    bassConsumer.accept(bass);
                    bass.close();
                } catch (ParseException | CelestaException | BassException e) {
                    ch.error(e.getMessage());
                    if (properties.isDebug())
                        e.printStackTrace();
                }
            }
        } finally {
            AnsiConsole.systemUninstall();
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


    enum Task {
        INIT,
        IMPORT,
        PLAN,
        APPLY;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        static Task getByString(String str) {
            return Arrays.stream(values()).filter(
                    v -> v.toString().equals(str)
            ).findFirst().orElse(null);
        }
    }
}
