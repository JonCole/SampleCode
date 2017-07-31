
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Logging {

    private static SimpleDateFormat sdf = createSDF();

    public static void writeLine()
    {
        writeLine("");
    }

    public static void writeLine(String str)
    {
        System.out.println(str);
    }

    public static void writeLine(String str, Object...args)
    {
        System.out.println(String.format(str, args));
    }

    public static void write(String str)
    {
        System.out.print(str);
    }

    public static void logException(Exception ex)
    {
        ex.printStackTrace(System.out);
    }

    public static String getPrefix()
    {
        String now = sdf.format(new Date());
        return String.format("[%s UTC] ", now);
    }

    private static SimpleDateFormat createSDF()
    {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        s.setTimeZone(TimeZone.getTimeZone("UTC"));
        return s;
    }
}
