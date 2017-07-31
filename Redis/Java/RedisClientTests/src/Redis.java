import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Redis {

    private static IRedisClient client;

    public static void initialize(IRedisClient client)
    {
        Redis.client = client;
    }

    public static IRedisClient getClient()
    {
        return client;
    }

    public static Timer startPingTimer(int secondsFrequency)
    {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Logging.write("P");
                    getClient().ping();
                } catch (Exception ex) {
                    //do nothing.  Non-fatal if a ping command errors out.
                }
            }
        }, 0, secondsFrequency*1000);
        return timer;
    }
}
