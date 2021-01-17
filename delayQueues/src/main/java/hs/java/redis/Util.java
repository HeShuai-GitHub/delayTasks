package hs.java.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

/**
 * @program: delayTasks
 * @description: Jedis工具类,这里简单写一个工具类类，对一些常量进行一下封装
 *              另外需要提示的是，目前连接Redis使用更多的是Lettuce，因为Lettuce是基于Netty的，这里为了例子简单，就使用Jedis了
 * @author: hs
 * @create: 2021-01-15 20:46
 **/
public class Util {

    private final static String REDIS_HOST = "127.0.0.1";

    private final static Integer REDIS_PORT = 6379;

    public final static String DELAY_TASK_QUEUE = "DelayTask";

    public static Jedis getJedis(){
        JedisShardInfo jedisShardInfo = new JedisShardInfo(new HostAndPort(REDIS_HOST, REDIS_PORT));
        jedisShardInfo.setPassword("123");
        return new Jedis(jedisShardInfo);
    }
}
