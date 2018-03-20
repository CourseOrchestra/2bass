package ru.curs.bass;

import info.macias.kaconf.Configurator;
import info.macias.kaconf.ConfiguratorBuilder;
import info.macias.kaconf.sources.JavaUtilPropertySource;
import ru.curs.celesta.*;
import ru.curs.celesta.score.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;


/**
 * Hello world!
 */
public class App {

    private static final String PROPERTIES_ENV_KEY = "BASS_PROPERTIES";

    private static final String HELP =
            "Usage: bass <command> [properties file]\n"
                    + "Available commands are:\n"
                    + "\tinit\t\t Init system schema\n"
                    + "\tapply\t\t Build or change database structure\n"
                    + "\timport\t\t Import actual database state to SQL scripts\n"
                    + "\tplan\t\t Generate and show DDL execution plan\n"
                    + "Required setup properties are:\n"
                    + "\tscore.path\t\t Path to SQL scripts\n"
                    + "\tjdbc.url\t\t JDBC connection URL\n"
                    + "\tjdbc.username\t\t Database user name\n"
                    + "\tjdbc.password\t\t Database password\n";

    private static final Map<String, Consumer<Bass>> TASKS = new HashMap<>();

    static {
        TASKS.put(Task.INIT.toString(), Bass::initSystemSchema);
        TASKS.put(Task.IMPORT.toString(), Bass::toString); //TODO:
        TASKS.put(Task.PLAN.toString(), Bass::outputDdlScript);
        TASKS.put(Task.APPLY.toString(), Bass::updateDb);
    }

    public static void main(String[] args) throws CelestaException, ParseException {
        System.out.println("This is 2bass.");

        if (args.length == 0) {
            System.out.println("No command was specified.\n" + HELP);
            return;
        }
        String task = args[0];
        Consumer<Bass> bassConsumer = TASKS.get(task);

        if (bassConsumer == null) {
            System.out.println("Invalid command was specified.\n" + HELP);
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
                System.out.printf("Properties file %s does not exists or cannot be read.%n%s",
                        propertiesFile.getAbsolutePath(), HELP);
                return;
            }
            AppProperties properties = readProperties(propertiesFile);
            properties.setTask(Task.getByString(task));

            Bass bass = new Bass(properties);
            bassConsumer.accept(bass);
            bass.close();
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
