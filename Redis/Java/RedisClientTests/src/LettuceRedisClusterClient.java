import com.lambdaworks.redis.*;
import com.lambdaworks.redis.cluster.*;
import com.lambdaworks.redis.cluster.api.*;
import com.lambdaworks.redis.cluster.api.sync.*;

public class LettuceRedisClusterClient implements IRedisClient {

    private RedisInstance redisInstance;
    private StatefulRedisClusterConnection<String, String> connection;
    private Object syncObject = new Object();

    public LettuceRedisClusterClient(RedisInstance instance)
    {
        redisInstance = instance;
    }

    public String getHostName() {return redisInstance.getHostname(); }

    public String get(String key)
    {
        try {
            return commands().get(key);
        } catch (Exception ex) {
            LettuceRedisClient.LogError(ex);
        }
        return null;
    }

    public void set(String key, String value)
    {
        try {
            commands().set(key, value);
        } catch (Exception ex) {
            LettuceRedisClient.LogError(ex);
        }
    }

    public String info()
    {
        try {
            return commands().info();
        } catch (Exception ex) {
            LettuceRedisClient.LogError(ex);
        }
        return "";
    }

    public void ping()
    {
        try{
            commands().ping();
        } catch (Exception ex) {
            LettuceRedisClient.LogError(ex);
        }
    }

    private RedisClusterClient getRedisClient()
    {

        RedisURI uri = RedisURI.builder()
                .withHost(redisInstance.getHostname())
                .withPassword(redisInstance.getPassword())
                .withPort(6380)
                .withSsl(true)
                .withDatabase(0)
                .build();
        RedisClusterClient client = RedisClusterClient.create(LettuceRedisClient.getClientResources(), uri);

        ClusterClientOptions options = ClusterClientOptions.builder()
                .validateClusterNodeMembership(false)
                .autoReconnect(true)
                .build();
        client.setOptions(options);
        return client;
    }

    protected StatefulRedisClusterConnection<String, String> getConnection()
    {
        if (connection == null) {
            synchronized (syncObject)
            {
                if (connection == null)
                {
                    RedisClusterClient client = getRedisClient();
                    connection = client.connect();
                    TrySetClientName();
                }
            }
        }

        return connection;
    }

    private RedisClusterCommands<String, String> commands()
    {
        return getConnection().sync();
    }

    private void TrySetClientName()
    {
        try {
            commands().clientSetname(Program.AppName + ":LettuceCluster");
        } catch(Exception ex){
            Logging.logException(ex);
        }
    }
}
