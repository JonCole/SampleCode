import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class LatencyPercentileTests {

    private static AtomicInteger threadCounter = new AtomicInteger(0);
    private static ArrayList<Double>  aggregatedData = new ArrayList<Double>();
    private static DecimalFormat dblFormat = new DecimalFormat("###.#");

    public static void Run(int threads, int iterations) {

        ArrayList<WorkerThread> list = new ArrayList<WorkerThread>();

        try {
            Logging.writeLine("Starting %d thread(s), %d iteration(s), using client %s", threads, iterations, Redis.getClient().getClass().getSimpleName());

            for(int i = 0; i < threads; i++)
                list.add(startLoadTest(iterations));

            Thread.sleep(1000);
            Logging.writeLine("\r\nwaiting for iterations to complete...");

            while(list.size() > 0)
            {
                WorkerThread t = list.remove(0);
                while (t.isAlive())
                {
                    Logging.write(".");
                    Thread.sleep(1000);
                }
            }

            Logging.writeLine("\r\nResult aggregation complete...");

            printResults();

        }
        catch(Exception ex) {

            Logging.logException(ex);
        }
    }

    private static void printResults() {

        Logging.writeLine( "--------------------------------------------------");
        Collections.sort(aggregatedData);

        int count = aggregatedData.size();

        Logging.writeLine(String.format("Request Count: %d", count));

        Logging.writeLine(getPercentile(50.0));
        Logging.writeLine(getPercentile(80.0));
        Logging.writeLine(getPercentile(90.0));
        Logging.writeLine(getPercentile(95.0));
        Logging.writeLine(getPercentile(98.0));
        Logging.writeLine(getPercentile(99.0));
        Logging.writeLine(getPercentile(99.5));
        Logging.writeLine(getPercentile(99.9));
        Logging.writeLine(getPercentile(100.0));
        Logging.writeLine( "--------------------------------------------------");
    }


    private static String getPercentile(Double percentile)
    {
        int count = aggregatedData.size();
        int index = (int)Math.floor((percentile/100) * count);

        if (index == count)
            index = count - 1;

        Double val = aggregatedData.get(index);
        return String.format("%5sth Percentile   : %sms", dblFormat.format(percentile), dblFormat.format(val));
    }

    private static WorkerThread startLoadTest(int iterations)
    {
        Worker work = new Worker(){
            @Override
            public void run() {

                ArrayList<Double> times = new ArrayList<Double>();
                int threadId = threadCounter.incrementAndGet();
                Logging.write("|" + threadId);
                String key = "foo:" + Program.AppName + ":" + Integer.toString(threadId);
                String value = new Date().toString();

                int warmUpCount = Math.max(iterations / 10, 300);

                Redis.getClient().set(key, value);

                //Logging.writeLine("\r\nStarting warmup with %d iterations.", warmUpCount);
                for(int i = 0; i < warmUpCount; i ++)
                    Redis.getClient().get(key);


                //Logging.writeLine("\r\nStarting real latency tests...");
                for(int i = 0; i < iterations; i++){

                    //if (i % 1000 == 0)
                    //    Logging.write(String.format("[%d/%d]", i, iterations));
                    IRedisClient client = Redis.getClient();
                    long start = Stopwatch.startNew();
                    client.get(key);
                    Double duration = Stopwatch.elapsedMillis(start);
                    times.add(duration);
                    if (getStopRequested())
                        break;
                }

                Logging.write("#");
                AggregateResults(times);
            }
        };

        return WorkerThread.start(work);
    }

    private static synchronized void AggregateResults(ArrayList<Double> threadResults)
    {
        aggregatedData.addAll(threadResults);
    }
}
