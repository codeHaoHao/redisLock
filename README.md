# redisLock
构建分布式redis分布式锁，简易不可重入的锁和可重入的锁
## 项目结构
<image align='center' width='50%' height='50%' src='https://github.com/codeHaoHao/readME-file/blob/master/redis-file/project_structure.png'/>
<li>其中Jedis为自定义的jedis操作接口，并没有写具体的实现</li>
<li>RedisLock为自定义实现锁的顶层接口</li>
<li>ReentrantRedisLock为可重入redis锁</li>
<li>UnReentrantRedisLock为不可重入的简易redis锁</li>
<h3> ReentrantRedisLock 可重入redis锁</h3>
<h4>在redis中的结构</h4>
可重入锁在redis中设计的结构如下图所示：
<image align='center' width='50%' height='50%' src='https://github.com/codeHaoHao/readME-file/blob/master/redis-file/reentrant-contruct.png'/>
<p>用了redis的hash结构来存储锁的相关信息，其中holder表示持有者，holder对应的value为UUID生成的identifier,identifier为线程的标识，每个线程都对应一个identifier。reenterCount为重入次数。</p>
不可重入锁在redis中设计的结构如下图所示：
<image align='center' width='50%' height='50%' src='https://github.com/codeHaoHao/readME-file/blob/master/redis-file/unreentrant-constuct.png'>
<p>不可重入锁主要运用redis的setnx方法实现</p>
