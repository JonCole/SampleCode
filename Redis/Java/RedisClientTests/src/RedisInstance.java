import org.springframework.beans.FatalBeanException;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class RedisInstance {
    static Dictionary<String, RedisInstance> map = new Hashtable<>();
    private String hostname;
    private String key;

    private RedisInstance(String h, String k)
    {
        hostname = h;
        key = k;
    }

    public String getHostname()
    {
        return hostname;
    }

    public String getPassword()
    {
        return key;
    }

    public static RedisInstance GeoReplicated() { return map.get("GeoReplicated"); }
    public static RedisInstance Clustered() { return map.get("Clustered"); }
    public static RedisInstance StandardC1() { return map.get("StandardC1"); }
    public static RedisInstance PremiumNonClustered() { return map.get("PremiumNonClustered"); }

    public static void loadFromFile(Path filePath)
    {
        try( BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))) {

            String line;
            while ((line = reader.readLine()) !=  null) {
                if (line.length() > 0) {
                    String[] tokens = line.split(":", 3);
                    map.put(tokens[0], new RedisInstance(tokens[1], tokens[2]));
                }
            }
        }
        catch( IOException ex){
            Logging.writeLine("Error while reading file %s", filePath);
            Logging.logException(ex);
            throw new FatalBeanException(ex.getMessage());
        }
    }
}
