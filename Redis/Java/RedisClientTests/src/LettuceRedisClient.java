import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.event.EventPublisherOptions;
import com.lambdaworks.redis.metrics.CommandLatencyCollector;
import com.lambdaworks.redis.metrics.CommandLatencyCollectorOptions;
import com.lambdaworks.redis.metrics.CommandLatencyId;
import com.lambdaworks.redis.metrics.CommandMetrics;
import com.lambdaworks.redis.protocol.ProtocolKeyword;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.springframework.beans.FatalBeanException;

import java.io.*;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LettuceRedisClient implements IRedisClient {

    private RedisInstance redisInstance;
    private StatefulRedisConnection<String, String> connection;
    private Object syncObject = new Object();
    private RedisCommands<String, String> commands;

    public LettuceRedisClient(RedisInstance instance)
    {
        redisInstance = instance;
    }

    public String getHostName() {return redisInstance.getHostname(); }

    public String get(String key)
    {
        try {
            return getCommands().get(key);
        } catch (Exception ex) {
            LogError(ex);
        }
        return null;
    }

    public String info()
    {
        try {
            return getCommands().info();
        } catch(Exception ex) {
            LogError(ex);
        }
        return "";
    }

    public void set(String key, String value)
    {
        try {
            getCommands().set(key, value);
        } catch(Exception ex) {
            LogError(ex);
        }
    }

    public static void LogError(Exception ex)
    {
        if (ex instanceof RedisCommandTimeoutException) {
            Logging.write("T");
        } else if (ex instanceof RedisCommandExecutionException) {
            Logging.write("E");
        } else if (ex instanceof RedisConnectionException) {
            Logging.write("C");
        } else if (ex instanceof IOException){
            Logging.write("C");
        } else {
            Logging.logException(ex);
            throw new FatalBeanException(ex.getMessage(), ex);  // unexpected exception type, so abort test for investigation
        }
    }

    public static DefaultClientResources getClientResources()
    {
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        threadPoolSize *= 2;

        DefaultClientResources resources = DefaultClientResources.builder()
                .ioThreadPoolSize(threadPoolSize)
                .computationThreadPoolSize(threadPoolSize)
                .build();
        Logging.writeLine("DefaultClientResources - ioThreads: %d, computeThreads: %d", resources.ioThreadPoolSize(), resources.computationThreadPoolSize());
        return resources;
    }

    public void ping()
    {
        try{
            getCommands().ping();
        } catch(Exception ex) {
            LogError(ex);
        }
    }

    private RedisClient getRedisClient()
    {


        RedisURI uri = RedisURI.builder()
                .withHost(redisInstance.getHostname())
                .withPassword(redisInstance.getPassword())
                .withPort(6380).withSsl(true)
                //.withPort(6379).withSsl(false)
                .withDatabase(0)
                .build();
        return RedisClient.create(getClientResources(), uri);
    }

    protected StatefulRedisConnection<String, String> getConnection()
    {
        if (connection == null) {
            synchronized (syncObject)
            {
                if (connection == null)
                {
                    Logging.write("$");
                    RedisClient client = getRedisClient();
                    connection = client.connect();
                    TrySetClientName();
                }
            }
        }

        return connection;
    }

    private RedisCommands<String, String> getCommands()
    {
        if (commands == null)
            commands = getConnection().sync();
        return commands;
    }

    private void TrySetClientName()
    {
        try {
            getCommands().clientSetname(Program.AppName + ":Lettuce");
        } catch(Exception ex){
            LogError(ex);
        }
    }
}
