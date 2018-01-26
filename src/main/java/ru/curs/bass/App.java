package ru.curs.bass;

import info.macias.kaconf.Configurator;
import info.macias.kaconf.ConfiguratorBuilder;
import info.macias.kaconf.sources.JavaUtilPropertySource;
import ru.curs.celesta.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;


/**
 * Hello world!
 */
public class App {

    private static final String PROPERTIES_ENV_KEY = "BASS_PROPERTIES";

    private static final String HELP = "Available commands are:\n\t\t"
            + "import\n\t\t"
            + "plan\n\t\t"
            + "apply";

    private static final Map<String, Consumer<Bass>> TASKS = new HashMap<>();

    static {
        TASKS.put(Task.IMPORT.toString(), Bass::toString); //TODO:
        TASKS.put(Task.PLAN.toString(), Bass::toString);  //TODO:
        TASKS.put(Task.APPLY.toString(), Bass::updateDb);
    }

    public static void main(String[] args) throws CelestaException {
        System.out.println("Hello World!");

        if (args.length == 0) {
            System.out.println("No command was specified.\n" + HELP);
            return;
        }
        String task = args[0];
        Consumer<Bass> bassConsumer = TASKS.get(task);

        if (bassConsumer == null) {
            System.out.println("Invalid command was specified.\n" + HELP);
        } else {
            String propertiesPath = System.getenv(PROPERTIES_ENV_KEY);
            AppProperties properties = readProperties(propertiesPath);
            Bass bass = new Bass(properties);
            bassConsumer.accept(bass);
            bass.close();
        }
    }

    private static AppProperties readProperties(String propertiesPath) {

        File propertiesFile = new File(propertiesPath);

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
            throw new BassException("There is no properties file on path " + propertiesPath);
        } catch (IOException e) {
            throw new BassException(e);
        }

    }


    private enum Task {
        IMPORT,
        PLAN,
        APPLY;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
