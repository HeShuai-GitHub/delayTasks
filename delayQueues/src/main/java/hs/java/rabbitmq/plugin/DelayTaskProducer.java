package hs.java.rabbitmq.plugin;

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
 * @description: 封装延迟任务发布类
 * @author: hs
 * @create: 2021-01-16 11:43
 **/
public class DelayTaskProducer {

    public static void main(String[] args) {
        try {
            sendPlugins();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void sendPlugins() throws IOException, TimeoutException {
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
        for(int i=5; i>0; i--){
            // 设置单独消息中的header参数
            Map<String,Object> heardParam = new HashMap<String, Object>();
            // 设置x-delay消息过期时间
            heardParam.put("x-delay",1000*2*i);
            /**
             * 为单独任务设置过期时间
             */
            AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder().headers(heardParam);
            String task = "我是任务"+i;
            channel.basicPublish(RabbitmqConfig.EXCHANGE_DEAD_DELAY,RabbitmqConfig.DELAY_QUEUE_RoutingKey,properties.build(),task.getBytes(Charset.forName("utf-8")));
        }
        // 关闭连接
        channel.getConnection().close();
    }
}
