public class Utils {
    public static String GenerateValue(int size)
    {
        StringBuffer value = new StringBuffer();
        for(int i = 0; i < size; i++)
            value.append("a");

        return value.toString();
    }
}
