import java.util.*;
import java.util.concurrent.atomic.*;

public class PooledRedisClient implements IRedisClient{

    ArrayList<IRedisClient> clients;
    AtomicInteger roundRobin = new AtomicInteger(0);

    public PooledRedisClient(ArrayList<IRedisClient> clients) {
        this.clients = clients;
    }

    @Override
    public void set(String key, String value) {
        getClient().set(key, value);
    }

    @Override
    public String get(String key) {
        return getClient().get(key);
    }

    @Override
    public void ping() {
        getClient().ping();
    }

    public String info() { return getClient().info(); }

    private IRedisClient getClient()
    {
        int index = roundRobin.incrementAndGet() % clients.size();

        //Logging.writeLine(Integer.toString(index));

        if (index > 100000)
            roundRobin.set(0);

        return clients.get(index);
    }
}

