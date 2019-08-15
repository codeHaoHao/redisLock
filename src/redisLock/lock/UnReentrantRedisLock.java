package redisLock.lock;

import java.util.UUID;

import redisLock.jedis.Jedis;

/**
 * 不可重入的简易的redis分布式锁
 * redis结构如下：
 * |-----lockname-----String----|
 * |    identifier              |
 * |----------------------------|
 * 实现主要运用redis的setnx方法
 * @author Li
 *
 */
public class UnReentrantRedisLock implements RedisLock{
  private Jedis jedis;

  @Override
  public String accquire_lock(String lockname, long acquire_timeout) {
    String identifier = UUID.randomUUID().toString();
    long now = System.currentTimeMillis();
    long end = now+acquire_timeout;
    while(System.currentTimeMillis()<end) {
      if(jedis.setnx(lockname, identifier)) {//如果获取锁成功
        jedis.expire(lockname, 1000);//设置锁的过期时间
        return identifier;
      }
      try {
        Thread.sleep(50);//每过50毫秒重新请求一次锁
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    return null;
  }

  @Override
  public String accquire_lock(String lockname) {
    return accquire_lock(lockname,1000);
  }

  @Override
  public boolean release_lock(String lockname, String identifier) {
    String r_identifier = jedis.get(lockname);
    if(r_identifier==null) return false;
    if(jedis.get(lockname).equals(identifier)) {
      try {
        jedis.watch(lockname);
        jedis.multi();
        jedis.delete(lockname);
        jedis.execute();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean release_lock(String lockname) {
    
    return false;
  }

}
