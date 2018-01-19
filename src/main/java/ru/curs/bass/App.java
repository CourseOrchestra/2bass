package ru.curs.bass;

import ru.curs.celesta.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Hello world!
 *
 */
public class App 
{

    private static final Map<String, Consumer<Bass>> TASKS = new HashMap<>();

    static {
        TASKS.put(Task.APPLY.toString(), Bass::updateDb);
    }

    public static void main( String[] args ) throws CelestaException
    {
        System.out.println( "Hello World!" );

        String task = task(args);
        Consumer<Bass> bassConsumer = TASKS.get(task);

        if (bassConsumer == null) {
            throw new BassException("Invalid command was specified");
        }

        AppProperties properties = readProperties();
        Bass bass = new Bass(properties);
        bassConsumer.accept(bass);
    }

    private static String task(String[] args) {
        if (args.length == 0)
            throw new BassException("Command wasn't specified");

        return args[0];
    }


    private static AppProperties readProperties() { //TODO!!!
        AppProperties properties = new AppProperties();
        properties.setScorePath("score");
        properties.setJdbcUrl("jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1");
        return properties;
    }

    private enum Task {
        APPLY;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
