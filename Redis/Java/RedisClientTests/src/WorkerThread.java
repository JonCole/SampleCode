import com.sun.istack.internal.NotNull;

public class WorkerThread extends Thread
{
    private Worker doWork;

    public Worker getWorker()
    {
        return doWork;
    }

    public WorkerThread (@NotNull Worker work)
    {
        doWork = work;
    }

    @Override
    public void run() {
        doWork.run();
    }

    public static WorkerThread start(Worker work)
    {
        WorkerThread t = new WorkerThread(work);
        t.start();
        return t;
    }
}



