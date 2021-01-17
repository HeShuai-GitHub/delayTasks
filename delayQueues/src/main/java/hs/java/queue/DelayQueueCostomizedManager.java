package hs.java.queue;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: delayTasks
 * @description: 自定义延迟队列管理类
 * @author: hs
 * @create: 2021-01-13 20:09
 **/
public class DelayQueueCostomizedManager {
    /**
     * 核心线程，长期保持的线程数——自定义线程池所用参数
     */
    private final static Integer COREPOOLSIZE = 10;
    /**
     * 最大线程数——自定义线程池所用参数
     */
    private final static Integer MAXIMUMPOOLSIZE = 100;
    /**
     * 闲置线程存活时间——自定义线程池所用参数
     */
    private final static Long KEEPALIVETIME = 50L;
    /**
     * 线程池执行器
     */
    private ExecutorService executorService;
    /**
     * 守护线程，由此线程去维护执行延时队列操作
     */
    private Thread daemonThread;
    /**
     * 延时队列存放位置
     */
    private DelayQueue<DelayMessage<?>> delayQueue;
    /**
     * 单例模式——饿汉模式
     */
    private static DelayQueueCostomizedManager instance = new DelayQueueCostomizedManager();

    private DelayQueueCostomizedManager(){
        // 创建线程池执行器
        executorService = new ThreadPoolExecutor(COREPOOLSIZE,MAXIMUMPOOLSIZE,KEEPALIVETIME, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),new ThreadPoolExecutor.AbortPolicy());
        // 初始化延迟队列
        delayQueue = new DelayQueue<DelayMessage<?>>();
        init();
    }

    public void init(){
        // 自启后，监听延迟队列
        daemonThread = new Thread(() -> {
            execute();
        });
        daemonThread.setName("守护线程，以此线程去执行");
        // 因为是测试，所以将设置守护线程注释掉，否则main线程结束，守护线程也随之关闭，这样就看不到延迟队列的效果了
        // daemonThread.setDaemon(true);
        daemonThread.start();
    }
    // 单例，饿汉模式
    public static DelayQueueCostomizedManager getInstance(){
        return instance;
    }

    /**
     * 监听延迟队列
     */
    private void execute(){
        while (true){
            // 获得当前进程中线程痕迹
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            System.out.println("当前存活线程数量:" + map.size());
            System.out.println("当前延时任务数量:" + delayQueue.size());
            try {
                // 获得延迟队列中的消息，必须消息到期才可以被获取，否则等待
                DelayMessage<?> delayMessage = delayQueue.take();
                if(null != delayMessage){
                    Runnable run = delayMessage.getTask();
                    if(null == run){
                        continue;
                    }
                    // 执行任务
                    executorService.execute(run);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 向延迟队列中添加任务
     * @param task 任务
     * @param time 延迟时长
     * @param unit 延迟时长单位
     */
    public void put(Runnable task, Long time, TimeUnit unit){
        /**
         * TimeUnit：提供时间转换功能
         * NANOSECONDS：时间单位，千分之一微分，也就是十亿分之一一秒
         */
        Long timeout= TimeUnit.NANOSECONDS.convert(time,unit);
        DelayMessage<?> delayMessage = new DelayMessage<>(timeout,task);
        delayQueue.put(delayMessage);
    }

    /**
     * 移除延迟队列中任务
     * @param delayMessage 移除任务
     * @return
     */
    public Boolean removeTask(DelayMessage delayMessage){
        return delayQueue.remove(delayMessage);
    }

}
