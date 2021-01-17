package hs.java.rabbitmq.common;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import hs.java.rabbitmq.RabbitmqConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @program: delayTasks
 * @description: 封装延迟任务发布类，在这类中只是简单使用了Rabbitmq的消息过期机制以及死信交换机来完成了一个延迟任务的发布
 *              并且在这种实现方式中是有一个弊端的，也就是当发送第一条延迟任务延迟时间过长，第二条延迟任务延迟时间比较短，那么当第二条任务到期的时候，
 *              也不能转发到死信交换机，不能实现任务被执行，必须等待第一条任务到期并且转发后才会轮到第二条任务，这个是queue队列的特性决定的
 *              关于这个弊端的解决方案，有两种
 *              第一种，就是将不同延迟时长的任务放到不同的queue中，这样就不会存在前面的延迟时长比后面的长的问题了
 *              第二种，就是使用Rabbitmq社区提供的插件（rabbitmq_delayed_message_exchange），使用这个插件可以很轻松的完成这个问题
 *              但是无论上面哪一种都是有弊有利的，第一种，这会造成Rabbitmq中队列数量过多，第二种会有一定的性能损耗、对cluster支持的并不是很好；
 * @author: hs
 * @create: 2021-01-16 11:43
 **/
public class DelayTaskProducer {

    public static void main(String[] args) {
        try {
            send();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void send() throws IOException, TimeoutException {
        Map<String,Object> param = new HashMap<String, Object>();
        /**
         * 声明该Queue若因为消息过期、Queue达到上限、被消费者拒绝的消息等，将消息转发到下面配置的Exchange中
         */
        param.put("x-dead-letter-exchange", RabbitmqConfig.EXCHANGE_DEAD);
        /**
         * 配置死信Exchange消息的路由Key
         */
        param.put("x-dead-letter-routing-key", "routing_dead");
        /**
         * 调用工具类创建channel
         */
        Channel channel = RabbitmqConfig.getChannel();
        /**
         * 声明死信Exchange，死信Queue就由consumer那边去声明
         * Topic是Exchange的类型，这个很简单就是可以模式匹配的意思
         */
        channel.exchangeDeclare(RabbitmqConfig.EXCHANGE_DEAD, BuiltinExchangeType.TOPIC);
        /**
         * 声明一个queue，参数讲解一下吧
         * queue：queue名字
         * durable：是否进行持久化，若为true，当Rabbitmq服务重启后，queue也会随之恢复
         * exclusive：是否独占，这块简单说一下，就是当为true的时候，其他的connection不可以使用这个queue，否则报错
         * autoDelete：是否自动删除，若为true，则当有一个消费者监听当前queue并断开连接后，则queue自动删除（若没有任何消费者监听该queue，则该queue不会自动删除）
         * arguments：一些额外的参数，如：x-max-length、x-max-length-bytes、x-max-priority等，还有很多参数，因为本篇不是重点介绍这个，就不深入了，但是等下用到的参数会介绍对应含义的
         */
        channel.queueDeclare(RabbitmqConfig.DELAY_QUEUE,false,false,true,param);
        // 在类介绍的位置提到的弊端就是这里假如将循环改为倒序就可以很清晰的观察到了
        for(int i=1; i<5; i++){
            /**
             * 为单独任务设置过期时间
             */
            AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder().expiration(2*1000*i+"");
            String task = "我是任务"+i;
            channel.basicPublish("",RabbitmqConfig.DELAY_QUEUE,properties.build(),task.getBytes(Charset.forName("utf-8")));
        }
        // 关闭连接
        channel.getConnection().close();
    }
}
