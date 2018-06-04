package ru.curs.bass;

import info.macias.kaconf.ConfiguratorBuilder;
import info.macias.kaconf.sources.JavaUtilPropertySource;
import org.fusesource.jansi.AnsiConsole;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.score.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;


public final class App {

    private static final Map<String, Consumer<Bass>> COMMANDS = new HashMap<>();
    static ConsoleHelper consoleHelper = new ConsoleHelper(System.out);

    static {
        COMMANDS.put(Command.INIT.toString(), Bass::initSystemSchema);
        COMMANDS.put(Command.IMPORT.toString(), Bass::toString); //TODO:
        COMMANDS.put(Command.PLAN.toString(), Bass::outputDdlScript);
        COMMANDS.put(Command.APPLY.toString(), Bass::updateDb);
        COMMANDS.put(Command.VALIDATE.toString(), bass -> {
        });
    }

    private App() {

    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        OptionsParser optionsParser = new OptionsParser(consoleHelper);
        try {
            consoleHelper.info("This is 2bass.");

            if (args.length == 0) {
                consoleHelper.error("No command was specified.");
                optionsParser.help();
                return;
            }
            String cmd = args[0];
            Consumer<Bass> bassConsumer = COMMANDS.get(cmd);
            if (bassConsumer == null) {
                consoleHelper.error("Invalid command was specified.\n");
                optionsParser.help();
                return;
            }

            LinkedList<Properties> configSources = new LinkedList<>();
            try {
                Properties props = readOptionsFromArgs(args, optionsParser);
                configSources.add(props);
                String propertiesPath = props.getProperty(OptionsParser.PROPERTIES_FILE);
                if (propertiesPath != null) {
                    configSources.addFirst(readOptionsFromFile(propertiesPath));
                }
            } catch (BassException e) {
                consoleHelper.error(e.getMessage());
                return;
            }

            AppProperties properties = buildConfiguration(configSources);

            properties.setCommand(Command.getByString(cmd));

            try (Bass bass = new Bass(properties, consoleHelper)) {
                bassConsumer.accept(bass);
            } catch (ParseException | CelestaException | BassException e) {
                consoleHelper.error(e.getMessage());
                if (properties.isDebug())
                    e.printStackTrace();
            }

        } finally {
            AnsiConsole.systemUninstall();
            if (consoleHelper.isError()) {
                consoleHelper.sysExit(1);
            }
        }
    }

    private static Properties readOptionsFromArgs(String[] args, OptionsParser optionsParser) {
        return optionsParser.getProperties(Arrays.copyOfRange(args, 1, args.length));
    }

    private static Properties readOptionsFromFile(String propertiesPath) {
        File propertiesFile = new File(propertiesPath);
        if (!(propertiesFile.exists() && propertiesFile.canRead())) {
            throw new BassException(String.format("Properties file %s does not exists or cannot be read.%n",
                    propertiesFile.getAbsolutePath()));
        }
        try (FileInputStream fis = new FileInputStream(propertiesFile.getAbsolutePath())) {
            Properties propsFromFile = new Properties();
            propsFromFile.load(fis);
            return propsFromFile;
        } catch (IOException e) {
            throw new BassException(e);
        }
    }

    private static AppProperties buildConfiguration(List<Properties> sources) {
        AppProperties properties = new AppProperties();

        ConfiguratorBuilder cb = new ConfiguratorBuilder();
        for (Properties p : sources)
            cb.addSource(new JavaUtilPropertySource(p));

        cb.build().configure(properties);
        return properties;
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


