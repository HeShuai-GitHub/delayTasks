package hs.java.queue;

import java.util.concurrent.TimeUnit;

/**
 * @program: delayTasks
 * @description: 测试延迟队列运行情况
 * @author: hs
 * @create: 2021-01-13 20:23
 **/
public class Test {
    public static void main(String[] args) {
        DelayTask taskOne = new DelayTask("任务一");
        DelayTask taskTwo = new DelayTask("任务二");
        DelayTask taskThree = new DelayTask("任务三");
        DelayQueueCostomizedManager manager = DelayQueueCostomizedManager.getInstance();
        manager.put(taskOne,5L, TimeUnit.SECONDS);
        manager.put(taskTwo,1L, TimeUnit.SECONDS);
        manager.put(taskThree,2L, TimeUnit.SECONDS);
    }
}
