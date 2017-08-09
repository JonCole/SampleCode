import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.Date;

public class Program {

    public static String AppName = ManagementFactory.getRuntimeMXBean().getName();
    public static void main(String[] args) {

        try {
            printSystemInfo();


            Path filePath = Paths.get(System.getProperty("user.dir"), "RedisInstances.txt");
            RedisInstance.loadFromFile(filePath);

            CommandLineArgs options = CommandLineArgs.parse(args);

            IRedisClient client = options.getClient();

            Redis.initialize(client);

            Logging.writeLine("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            Logging.writeLine("Starting Scenario: %s, threads=%d, iterations=%d, host=%s",
                    options.getScenario(),
                    options.getThreadCount(),
                    options.getIterationCount(),
                    client.getHostName());
            Logging.writeLine("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            ITestScenario scenario = getTestScenario(options.getScenario());

            scenario.run(options);

        }
        catch( Exception ex)
        {
            Logging.logException(ex);
        }
    }

    private static void printSystemInfo() {
        Logging.writeLine("Working Dir: %s", System.getProperty("user.dir"));
        Logging.writeLine("Available Processors: %d", Runtime.getRuntime().availableProcessors());
    }

    public static ITestScenario getTestScenario(String scenarioName)
    {
        ITestScenario result;
        switch(scenarioName.toLowerCase()) {
            case "load":
                result = new LoadTests();
                break;
            case "latency":
                result = new LatencyPercentileTests();
                break;
            default:
                throw new IllegalArgumentException("Unknown Scenario: " + scenarioName);
        }
        //IdleConnectionTests.run(11*60);
        //simpleGetTest();
        //RedisConsole();

        return result;
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


