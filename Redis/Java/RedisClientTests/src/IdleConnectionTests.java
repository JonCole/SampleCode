import java.util.ArrayList;

public class IdleConnectionTests {

    public static void Run(int idleSeconds) {
        int concurrency = 10;
        ArrayList<WorkerThread> list = new ArrayList<WorkerThread>();

        try {
            for (int i = 0; i < concurrency; i++) {
                list.add(LoadTests.startLoadTest());
            }

            Redis.startPingTimer(60);

            Thread.sleep(10000);

            // Close all of the worker threads so that the connections go idle
            while(list.size() >0) {
                list.get(0).getWorker().stop();
                Thread.sleep(100);
                list.remove(0);
            }

            // Wake up periodically and use the connection to see if it succeeds.
            while(true)
            {
                double minutes = idleSeconds / 60.0;
                String logEntry = String.format("\r\n%s Sleeping for %s minute(s).", Logging.getPrefix(), Double.toString(minutes));
                Logging.writeLine(logEntry);
                Thread.sleep(idleSeconds * 1000);
                Logging.write("^");
                String result = Redis.getClient().get("foo");
                Logging.write(String.format("foo=%s", result));
                idleSeconds += idleSeconds;
            }
        }
        catch(Exception ex) {

            Logging.logException(ex);
        }
    }
}
