package hs.java.redis;

import redis.clients.jedis.Jedis;

/**
 * @program: delayTasks
 * @description: 将任务保存在对应redis中
 * @author: hs
 * @create: 2021-01-15 20:56
 **/
public class DelayTaskProducer {

    public static void produce(String task, Double timeStamp){
        try(Jedis jedis = Util.getJedis()){
            /**
             * 添加一个任务
             */
            jedis.zadd(Util.DELAY_TASK_QUEUE,System.currentTimeMillis()+timeStamp,task);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
