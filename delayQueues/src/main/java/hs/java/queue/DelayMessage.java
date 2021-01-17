package hs.java.queue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @program: delayTasks
 * @description: 延时队列中消息体，对任务进行了封装
 * @author: hs
 * @create: 2021-01-10 20:17
 **/
public class DelayMessage<T extends Runnable> implements Delayed {

    private final Long time;

    private final T task;

    public DelayMessage(Long time, T task) {
        this.time = System.nanoTime()+time;
        this.task = task;
    }

    public Long getTime() {
        return time;
    }

    public T getTask() {
        return task;
    }

    /**
     * 根据给定的时间单位，给出当前任务的剩余延迟时间
     * @param unit TimeUnit
     * @return 返回剩余延迟时间
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time-System.nanoTime(),TimeUnit.NANOSECONDS);
    }

    /**
     * 比较方法，未到期返回1，消息到期返回0或-1
     * @param o
     * @return
     */
    @Override
    public int compareTo(Delayed o) {
        DelayMessage other = (DelayMessage) o;
        Long diff = this.time-other.time;
        if(diff>0){
            return 1;
        }else if(diff<0){
            return -1;
        }
        return 0;
    }
}
