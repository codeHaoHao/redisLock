package redisLock.lock;



public interface RedisLock {
  /**
   * 尝试获取redis的分布式锁
   * @param lockname
   * @param acquire_timeout 请求重试时间，单位默认为毫秒
   * @return 锁的value
   */
  String accquire_lock(String lockname,long acquire_timeout);
  /**
   * 尝试获取redis的分布式锁,默认请求时间为1000毫秒
   * @param lockname
   * @return 锁的value
   */
  String accquire_lock(String lockname);
  /**
   * 释放锁
   * @param lockname 锁名称
   * @param identifier 当前锁的value
   * @return
   */
  boolean release_lock(String lockname,String identifier);
  boolean release_lock(String lockname);
}
