public class Stopwatch {
    public static long startNew()
    {
        return System.nanoTime();
    }

    public static Double elapsedMillis(long startNS)
    {
        Double nanoToMillis = 0.000001;
        return (System.nanoTime() - startNS) * nanoToMillis;
    }

}
