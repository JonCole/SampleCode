import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.beans.FatalBeanException;

import java.io.*;

public class LettuceRedisClient implements IRedisClient {

    private RedisInstance redisInstance;
    private StatefulRedisConnection<String, String> connection;
    private Object syncObject = new Object();
    private RedisCommands<String, String> commands;

    public LettuceRedisClient(RedisInstance instance)
    {
        redisInstance = instance;
    }

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
            throw new FatalBeanException(ex.getMessage());
        }
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
                .withPassword(redisInstance.getKey())
                .withPort(6380).withSsl(true)
                //.withPort(6379).withSsl(false)
                .withDatabase(0)
                .build();
        return RedisClient.create(uri);
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
