
public abstract class Worker implements Runnable
{
    private boolean stopRequested;
    public boolean getStopRequested()
    {
        return stopRequested;
    }
    public void stop()
    {
        stopRequested = true;
    }
}