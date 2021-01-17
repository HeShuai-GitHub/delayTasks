package hs.java.queue;

/**
 * @program: delayTasks
 * @description: 实际业务执行类
 * @author: hs
 * @create: 2021-01-10 20:14
 **/
public class DelayTask implements Runnable{

    private String taskName;

    public DelayTask(String name){
        taskName = name;
    }

    @Override
    public void run() {
        System.out.println("********START************");
        System.out.println("当前线程名：" + Thread.currentThread().getName());
        System.out.println("当前任务名：" + taskName);
        System.out.println("实际业务执行位置");
        System.out.println("********END************");
    }
}
