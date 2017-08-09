import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisBusyException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.net.SocketTimeoutException;

public class JedisClusterClient implements IRedisClient {

    private RedisInstance redisInstance;
    private Object lock = new Object();
    private JedisCluster cluster;
    private JedisPoolConfig config;
    private int connectTimeout = 5000;
    private int operationTimeout = 5000;
    private int port = 6379;
    private int maxConnections = 400;

    public JedisClusterClient(RedisInstance instance)
    {
        redisInstance = instance;
    }

    public String getHostName() {return redisInstance.getHostname(); }

    @Override
    public String get(String key) {
        try
        {
            return getInstance().get(key);
        } catch (Exception ex) {
            JedisRedisClient.LogError(ex);
        }
        return null;
    }

    @Override
    public void ping() {
        try
        {
            getInstance().ping();
        } catch (Exception ex) {
            JedisRedisClient.LogError(ex);
        }
    }

    public String info()
    {
        try
        {
            return getInstance().info();
        } catch (Exception ex) {
            JedisRedisClient.LogError(ex);
        }

        return "";
    }

    @Override
    public void set(String key, String value) {

        try
        {
            getInstance().set(key, value);
            //Logging.write("+");
        } catch (Exception ex) {
            JedisRedisClient.LogError(ex);
        }
    }

    public JedisCluster getInstance() {
        if (cluster == null) { // avoid synchronization lock if initialization has already happened
            synchronized(lock) {
                if (cluster == null) { // don't re-initialize if another thread beat us to it.

                    HostAndPort node = new HostAndPort(redisInstance.getHostname(), port);
                    int maxAttempts = 3; // Max retries due to redirects...
                    JedisPoolConfig poolConfig = getPoolConfig();
                    //String clientName =  Program.AppName + ":Jedis";
                    cluster = new JedisCluster(node, connectTimeout, operationTimeout, maxAttempts, redisInstance.getPassword(), poolConfig);
                }
            }
        }
        return cluster;
    }

    private JedisPoolConfig getPoolConfig() {
        if (config == null) {
            JedisPoolConfig poolConfig = new JedisPoolConfig();

            // Each thread trying to access Redis needs its own Jedis instance from the pool.
            // Using too small a value here can lead to performance problems, too big and you have wasted resources.

            poolConfig.setMaxTotal(maxConnections);
            poolConfig.setMaxIdle(maxConnections);

            // Using "false" here will make it easier to debug when your maxTotal/minIdle/etc settings need adjusting.
            // Setting it to "true" will result better behavior when unexpected load hits in production
            poolConfig.setBlockWhenExhausted(true);

            // How long to wait before throwing when pool is exhausted
            poolConfig.setMaxWaitMillis(5000);

            // This controls the number of connections that should be maintained for bursts of load.
            // Increase this value when you see pool.getResource() taking a long time to complete under burst scenarios
            poolConfig.setMinIdle(50);

            config = poolConfig;
        }

        return config;
    }
}
