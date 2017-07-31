public interface IRedisClient {
    String get(String key);
    void set(String key, String value);
    void ping();
    String info();
}
