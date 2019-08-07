package redisLock.jedis;

public interface Jedis {
  int set(String key,String value);
  String get(String key);
  long incr(String key);
  long decr(String key);
  long expire(String key,long milliseconds);
  long hset(String key,String field,String value);
  boolean hsetnx(String key,String field,String value);
  long hincrBy(String key,String field,long value);
  String hget(String key,String field);
  boolean setnx(String key,String value);
  int delete(String key);
  void watch(String key) throws Exception;
  void multi();
  void execute();
}
