import com.sun.istack.internal.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTests implements ITestScenario {
    private  static AtomicInteger threadCounter = new AtomicInteger(0);
    private static int writePercent = 10;

    public void run(@NotNull CommandLineArgs options) {

        int threadCount = options.getThreadCount();
        int iterationCount = options.getIterationCount();

        ArrayList<WorkerThread> list = new ArrayList<WorkerThread>();

        try {
            for (int i = 0; i < threadCount; i++) {
                list.add(startLoadTest(iterationCount));
            }

            while(true)
            {
                Thread.sleep(1000);
                boolean done = true;

                for(WorkerThread t : list)
                {
                    done &= !t.isAlive();
                }
                if (done)
                    return;
            }
        }
        catch(Exception ex) {

            Logging.logException(ex);
        }
    }

    public static WorkerThread startLoadTest()
    {
        return startLoadTest(Integer.MAX_VALUE);
    }

    private static WorkerThread startLoadTest(int iterationCount)
    {
        Worker work = new Worker(){
            @Override
            public void run() {
                Random rand = new Random();
                int count = 0;
                int threadId = threadCounter.incrementAndGet();
                Logging.write("|" );

                String key = "foo:" + Program.AppName + ":" + Integer.toString(threadId);
                String value = new Date().toString();

                while(true) {

                    if (count < 2 || rand.nextInt(100) < writePercent) {
                        value = new Date().toString();
                        Redis.getClient().set(key, value);
                        Logging.write("-");
                        if (iterationCount < 0)
                            count = 10; //for infinite case, stop setting value for every request.
                    }

                    if (iterationCount > 0)
                        count++;

                    String valueFromCache = Redis.getClient().get(key);
                    if (valueFromCache == null || !valueFromCache.equals(value))
                    {
                        Logging.write("M");
                    }
                    else
                        Logging.write(".");


                    if (getStopRequested() ||
                            (iterationCount > 0 && count >= iterationCount))
                    {
                        Logging.write("#");
                        return;
                    }


                }
            }
        };

        return WorkerThread.start(work);
    }
}
