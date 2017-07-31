import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.Date;

public class Program {

    public static String AppName = ManagementFactory.getRuntimeMXBean().getName();
    public static void main(String[] args) {

        try {
            Logging.writeLine("Working Dir: %s", System.getProperty("user.dir"));
            Path filePath = Paths.get(System.getProperty("user.dir"), "RedisInstances.txt");
            RedisInstance.loadFromFile(filePath);

            CommandLineArgs options = CommandLineArgs.parse(args);

            IRedisClient client = options.getClient();


            Redis.initialize(client);

            Logging.writeLine("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            Logging.writeLine("Starting Scenario: %s, threads=%d, iterations=%d", options.getScenario(), options.getThreadCount(), options.getIterationCount());
            Logging.writeLine("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            switch(options.getScenario().toLowerCase()) {
                case "load":
                    LoadTests.Run(options.getThreadCount(), options.getIterationCount());
                    break;
                case "latency":
                    LatencyPercentileTests.Run(options.getThreadCount(), options.getIterationCount());
                    break;
                default:
                    Logging.writeLine("UNKNOWN SCENARIO: " + options.getScenario());;
                    return;
            }
            //IdleConnectionTests.Run(11*60);
            //simpleGetTest();
            //RedisConsole();
        }
        catch( Exception ex)
        {
            Logging.logException(ex);
        }
    }

    public static void simpleGetTest(){
        Logging.writeLine("Connecting...");
        Redis.getClient();
        String value = new Date().toString();
        Logging.writeLine("Setting initial value=" + value);
        Redis.getClient().set("foo", value);
        Logging.writeLine("done");
        String result = Redis.getClient().get("foo");

        Logging.writeLine("Returned from cache: " + result);
    }

    private static void RedisConsole() {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                Logging.writeLine("");
                Logging.write(".> ");
                String line = console.readLine();
                String[] tokens = line.split(" ");

                if (tokens.length < 1)
                    return;
                switch (tokens[0].toLowerCase()) {
                    case "set":
                        if (tokens.length != 3)
                            continue;
                        Redis.getClient().set(tokens[1], tokens[2]);
                        break;
                    case "get":
                        if (tokens.length < 2)
                            continue;
                        Logging.writeLine( " " + Redis.getClient().get(tokens[1]));
                        break;
                    case "info":
                        if (tokens.length > 1)
                            continue;
                        Logging.writeLine( " " + Redis.getClient().info());
                        break;
                    default:
                        Logging.writeLine(String.format("Unknown command %s", tokens[0]));
                }
            }
        } catch ( IOException ex)        {
            Logging.logException(ex);
        }
    }
}


