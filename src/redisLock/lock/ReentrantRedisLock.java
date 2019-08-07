package redisLock.lock;

import java.util.UUID;

import redisLock.jedis.Jedis;
/**
 * 可重入的redis分布式锁
 * 在redis中的结构如下：
 * |----lockname--|----hash----|
 * |   holder     | identifier |
 * | reenterCount |    int     |
 * |---------------------------|
 * 1.其中holder为持有者，holder对应的value为UUID生成的identifier，
 * identifier为线程的标识，每个线程都对应一个identifier，用线程本地变量ThreadLocal存储
 * 2.reenterCount为重入次数，每重入获取锁一次就用hincrBy方法自增一次
 * @author Li
 *
 */
public class ReentrantRedisLock implements RedisLock{
  private Jedis jedis;
  private static ThreadLocal<String> identifiers = new ThreadLocal<String>();
  private final String HOLDER = "holder";
  private final String REENTERCOUNT = "reenterCount";
  
  public ReentrantRedisLock(Jedis jedis) {
    this.jedis = jedis;
  }
  
  public ReentrantRedisLock() {
  }
  @Override
  public String accquire_lock(String lockname) {
    return accquire_lock(lockname, 1000);
  }
  @Override
  public String accquire_lock(String lockname, long acquire_timeout) {
    long currentTime = System.currentTimeMillis();
    long end = currentTime+acquire_timeout;//结束等待获取锁时间
    String identifier = identifiers.get();//获取线程中存储的认证信息identifier
    if(identifier==null) {
      identifier = UUID.randomUUID().toString();
      identifiers.set(identifier);
    }
    if(isReentrant(lockname, identifier)) {//判断当前线程是否可以重入获取锁
      incrBy(lockname, REENTERCOUNT, 1);//重入获取锁把重入次数(reenterCount)+1
      return identifier;
    }
    while(System.currentTimeMillis()<end) {//在指定的等待时间里不断重新请求获取锁
      if(jedis.hsetnx(lockname, HOLDER, identifier)) {//获取锁成功
        jedis.expire(lockname, 1000);//设置过期时间，避免死锁线程持有锁
        jedis.hincrBy(lockname, REENTERCOUNT, 1);//并把重入次数置为1
        return identifier;
      }
      try {
        Thread.sleep(50);//当前线程未获取到锁，就50毫秒后再次请求获取锁
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
  @Override
  public boolean release_lock(String lockname, String identifier) {
    String r_identifier = jedis.hget(lockname, HOLDER);//获取当前锁的持有者的认证信息identifier
    if(r_identifier!=null&&r_identifier.equals(identifier)) {//判断是否是该锁的持有者
      long reenterCount = Long.parseLong(jedis.hget(lockname, REENTERCOUNT));//获取线程重入获取锁次数
      if(reenterCount>0) {//如果重入次数大于0
        decrBy(lockname, REENTERCOUNT);//减少重入次数
        return true;
      }else {//没有重入
        jedis.delete(lockname);//释放这个锁
        return true;
      }
    }
    return false;
  }
  @Override
  public boolean release_lock(String lockname) {
    return release_lock(lockname,identifiers.get());
  }
  /**
   * 重入参数自增
   * @param lockname
   * @param field
   * @param value
   */
  private void incrBy(String lockname,String field,long value) {
    try {
      jedis.watch(lockname);
      jedis.multi();
      jedis.hincrBy(lockname,REENTERCOUNT,1);
      jedis.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * 重入参数自减
   * @param lockname
   * @param field
   * @param value
   */
  private void decrBy(String lockname,String field) {
    try {
      jedis.watch(lockname);
      jedis.multi();
      jedis.hincrBy(lockname,REENTERCOUNT,-1);
      jedis.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * 判断是否可重入
   * @param lockname 锁的名称
   * @param identifier 线程独有的身份认证
   * @return
   */
  private boolean isReentrant(String lockname,String identifier) {
    String r_identifier = jedis.hget(lockname, HOLDER);
    if(r_identifier==null) {
      return false;
    }
    if(r_identifier.equals(identifier)) {//如果锁的持有者信息跟当前线程的认证信息identifier相同即可重入
      return true;
    }
    return false;
  }

  public Jedis getJedis() {
    return jedis;
  }

  public void setJedis(Jedis jedis) {
    this.jedis = jedis;
  }
  
}
