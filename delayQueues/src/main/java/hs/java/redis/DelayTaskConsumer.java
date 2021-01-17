package hs.java.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: delayTasks
 * @description: 实际任务执行位置
 * @author: hs
 * @create: 2021-01-15 21:04
 **/
public class DelayTaskConsumer {
    /**
     * 声明一个调度线程池，这里可以用调度框架替代。如：xxl-job
     */
    private static ScheduledExecutorService service =new ScheduledThreadPoolExecutor(5);

    public static void start(){
        /**
         * 开始执行任务
         * 第一个参数：实际任务的类
         * 第二个参数：第一次执行延时时间
         * 第三个参数：每次任务执行时间
         * 第四个参数：时间单位
         */
        service.scheduleWithFixedDelay(new DelayTaskHandle(),1,1, TimeUnit.SECONDS);
    }

    /**
     * 以内部类的方式来执行判断是否任务到期
     */
    private static class DelayTaskHandle implements Runnable{

        @Override
        public void run() {
            // 存放每次实际操作的任务，若任务在未执行完失败，则重新放入Redis中，防止因异常导致任务失败
            Tuple task = null;
            try(Jedis jedis = Util.getJedis()) {
                /**
                 * 查询redis中zset类型中的元素score根据一个范围进行赛选
                 * key: zset的key值
                 * min：score的最小值
                 * max：score的最大值
                 * 返回对象是Tuple，其实就是zset类型中score和element的封装对象
                 */
                Set<Tuple> tasks = jedis.zrangeByScoreWithScores(Util.DELAY_TASK_QUEUE,0,System.currentTimeMillis());
                if(null == tasks){
                    return;
                }
                for (Tuple taskTemp:tasks){
                    task = taskTemp;
                    // 为防止多线程的情况下，重复执行任务，则在每次执行前先删除元素，删除成功后则才开始执行
                    Long count = jedis.zrem(Util.DELAY_TASK_QUEUE,task.getElement());
                    if (null!=count && count==1){
                        // 任务实际执行位置
                        System.out.println("当前任务是："+task.getElement()+"，已经被执行！当前线程是："+Thread.currentThread().getName());
                        task = null;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                // 若报出异常，为防止任务丢失，则将任务重新放入redis中
                if(null != task){
                    DelayTaskProducer.produce(task.getElement(),task.getScore());
                }
            }
        }
    }
}
