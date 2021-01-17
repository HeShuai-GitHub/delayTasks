package hs.java.rabbitmq.plugin;

import com.rabbitmq.client.*;
import hs.java.rabbitmq.RabbitmqConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @program: delayTasks
 * @description: 封装延迟任务消费类
 * @author: hs
 * @create: 2021-01-16 11:43
 **/
public class DelayTaskConsumer {

    public static void main(String[] args) throws IOException, TimeoutException {
        // Exchange额外的参数
        Map<String,Object> exchangeParam = new HashMap<String, Object>();
        // 设置Exchange实际的类型，可以设置direct、topic、fanout等
        exchangeParam.put("x-delayed-type", "direct");
        /**
         * 调用工具类创建channel
         */
        Channel channel = RabbitmqConfig.getChannel();
        // 声明一个Exchange，并且将类型定义为x-delayed-message，持久化，不自动删除，真正的类型定义为direct
        channel.exchangeDeclare(RabbitmqConfig.EXCHANGE_DEAD_DELAY,RabbitmqConfig.EXCHANGE_TYPE_DELAY_PLUGINS,true,false,exchangeParam);
        /**
         * 声明一个queue，参数讲解一下吧
         * queue：queue名字
         * durable：是否进行持久化，若为true，当Rabbitmq服务重启后，queue也会随之恢复
         * exclusive：是否独占，这块简单说一下，就是当为true的时候，其他的connection不可以使用这个queue，否则报错
         * autoDelete：是否自动删除，若为true，则当有一个消费者监听当前queue并断开连接后，则queue自动删除（若没有任何消费者监听该queue，则该queue不会自动删除）
         * arguments：一些额外的参数，如：x-max-length、x-max-length-bytes、x-max-priority等，还有很多参数，因为本篇不是重点介绍这个，就不深入了，但是等下用到的参数会介绍对应含义的
         */
        channel.queueDeclare(RabbitmqConfig.DELAY_QUEUE_PLUGIN,false,false,true,null);
        // 绑定Exchange和Queue
        channel.queueBind(RabbitmqConfig.DELAY_QUEUE_PLUGIN,RabbitmqConfig.EXCHANGE_DEAD_DELAY,RabbitmqConfig.DELAY_QUEUE_RoutingKey);
        /**
         * 执行任务
         */
        channel.basicConsume(RabbitmqConfig.DELAY_QUEUE_PLUGIN,true,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body)
                    throws IOException
            {
                System.out.println("Consumer 当前执行的是："+new String(body));
            }
        });
        // 这里没有关闭连接，主要是希望consumer可以一直监听Queue
    }
}
