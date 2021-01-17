package hs.java.redis;

import hs.java.queue.DelayTask;

/**
 * @program: delayTasks
 * @description: 测试Redis延时任务使用情况
 * @author: hs
 * @create: 2021-01-15 21:31
 **/
public class Test {
    public static void main(String[] args) {
        DelayTaskProducer.produce("任务一",4*1000.0);
        DelayTaskProducer.produce("任务二",2*1000.0);
        DelayTaskProducer.produce("任务三",8*1000.0);
        DelayTaskProducer.produce("任务四",12*1000.0);
        DelayTaskProducer.produce("任务五",10*1000.0);
        // 模拟多服务处理
        for (int i=0; i<5;i++){
            DelayTaskConsumer.start();
        }
    }
}
