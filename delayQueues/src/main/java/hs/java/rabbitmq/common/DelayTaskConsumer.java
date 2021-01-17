package hs.java.rabbitmq.common;

import com.rabbitmq.client.*;
import hs.java.rabbitmq.RabbitmqConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @program: delayTasks
 * @description: 封装延迟任务消费类，实际执行任务
 * @author: hs
 * @create: 2021-01-16 11:43
 **/
public class DelayTaskConsumer {

    public static void main(String[] args) throws IOException, TimeoutException {
        /**
         * 获得channel
         */
        Channel channel = RabbitmqConfig.getChannel();
        /**
         * 声明Exchange，以防止consumer启动的时候，producer还未启动从而导致的Exchange不存在
         */
        channel.exchangeDeclare(RabbitmqConfig.EXCHANGE_DEAD, BuiltinExchangeType.TOPIC);
        /**
         * 随机生成一个Queue
         */
        String queue = channel.queueDeclare().getQueue();
        /**
         * 以RoutingKey绑定Exchange和Queue
         */
        channel.queueBind(queue,RabbitmqConfig.EXCHANGE_DEAD,"routing_dead");
        /**
         * 执行任务
         */
        channel.basicConsume(queue,true,new DefaultConsumer(channel){
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
