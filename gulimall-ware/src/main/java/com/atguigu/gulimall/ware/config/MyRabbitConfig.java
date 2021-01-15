package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: LDeng
 * @Date: 2021-01-07 21:14
 */
@Configuration
public class MyRabbitConfig {


    @Autowired
    RabbitTemplate rabbitTemplate;


//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message){
//
//    }
    /**
     * JSON 序列化对象配置
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
       return new TopicExchange("stock-event-exchange", true  ,  false);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue("stock.release.stock.queue",true,false,false);
    }

    @Bean
    public Queue stockDelayQueue(){
        Map<String,Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue",
                                Binding.DestinationType.QUEUE,
                        "stock-event-exchange",
                "stock.release.#",null);
    }

    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",null);
    }

//    /**
//     * 定制rabbitTemplate
//     */
//    @PostConstruct  //MyRabbitConfig对象创建完成以后执行这个方法
//    public void initRabbitTemplate() {
//        //服务器收到消息回调 ConfirmCallback
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//
//            /**
//             * 1、只要消息抵达服务器 ack=true
//             * @param correlationData 当前消息唯一关联的数据 这个是消息爱的唯一id
//             * @param ack 消息是否成功收到
//             * @param cause 失败的原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                //服务器收到了
//                System.out.println("confirmCallback："+correlationData+"--ack："+ack+"--cause："+cause);
//
//            }
//        });
//
//        //消息抵达队列回调 returnCallback
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            //消息没有投递给指定的队列，会触发这个回调
//
//            /**
//             *
//             * @param message 投递失败的消息详细信息
//             * @param replyCode 恢复的状态码
//             * @param replyText 恢复的文本内容
//             * @param exchange 当时发送给的交换机
//             * @param routingKey 路由键
//             */
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                System.out.println("returnCallback: Fail Message["+message+"]"
//                        +"--code"+replyCode
//                        +"--replyText:"+replyText
//                        +"---exchange:"+exchange
//                        +"--routingKey"+routingKey);
//            }
//        });
//
//        //消费端确认
//            //*默认是自动确认的， 只要消息接收到，客户端会自动确认，服务端就会移除这个消息
//            //
//    }
}
