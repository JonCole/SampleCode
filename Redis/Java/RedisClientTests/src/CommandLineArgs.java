import com.sun.istack.internal.NotNull;
import sun.tools.jar.CommandLine;

import java.util.ArrayList;

public class CommandLineArgs {
    private int threadCount = 4;
    private int iterationCount = 20000;
    private String scenario = "latency";
    private String client = "lettuce";

    public static CommandLineArgs parse(String [] args)
    {
        CommandLineArgs result = new CommandLineArgs();

        for(int i = 0; i < args.length; i++)
        {
            String currentArg = args[i];
            String [] tokens = currentArg.split(":", 2);
            String key = tokens[0];
            String value = tokens.length > 1 ? tokens[1] : null;

            switch(key)
            {
                case "-t":
                    result.threadCount = parseInt(value);
                    break;
                case "-i":
                    result.iterationCount = parseInt(value);
                    break;
                case "-s":
                    result.scenario = value;
                    break;
                case "-c":
                    result.client = value;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal command line entry: " + key);
            }
        }

        return result;
    }

    public int getThreadCount() { return threadCount; }
    public int getIterationCount() { return iterationCount; }
    public String getScenario() { return scenario; }

    public IRedisClient getClient() {
        IRedisClient result = null;

        switch(client.toLowerCase())
        {
            case "j":
            case "jedis":
                result = new JedisRedisClient(RedisInstance.StandardC1());
                break;
            case "j-p": //jedis with premium sku server
                result = new JedisRedisClient(RedisInstance.PremiumNonClustered());
                break;
            case "l":
            case "lettuce":
                result = new LettuceRedisClient(RedisInstance.StandardC1());
                break;
            case "l-p": //Lettuce with premium sku server
                result = new LettuceRedisClient(RedisInstance.PremiumNonClustered());
                break;
            case "lc":
                result = new LettuceRedisClusterClient(RedisInstance.Clustered());
                break;
            case "lp":
                result = getPooledClient(5, () -> new LettuceRedisClient(RedisInstance.StandardC1()));
                break;
            case "jc":
                result = new JedisClusterClient(RedisInstance.Clustered());
                break;
            default:
                throw new IllegalArgumentException("client unknown: " + client);
        }

        if (result == null) {
            result = new LettuceRedisClient(RedisInstance.StandardC1());
            //client = new LettuceRedisClusterClient(RedisInstance.Clustered);
            //client = new JedisRedisClient(RedisInstance.StandardC1);
            //client = getPooledClient(10, () -> new LettuceRedisClient(RedisInstance.StandardC1));
            //client = getPooledClient(10, () -> new JedisRedisClient(RedisInstance.StandardC1));
            Logging.writeLine("************ Using Default client: " + client.getClass().getSimpleName() + "************");
        }

        return result;
    }

    public static int parseInt(@NotNull String s) {
        if ( s.length() == 0 || s.compareToIgnoreCase("max") == 0)
            return Integer.MAX_VALUE;
        return Integer.parseInt(s);
    }

    private static IRedisClient getPooledClient(int size, @NotNull IRedisClientFactory factory) {
        ArrayList<IRedisClient> list = new ArrayList<>();
        for(int i = 0; i < size; i++)
            list.add(factory.create());

        return new PooledRedisClient(list);
    }
}
