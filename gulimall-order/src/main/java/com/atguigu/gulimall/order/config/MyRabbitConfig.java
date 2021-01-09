package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * @Author: LDeng
 * @Date: 2021-01-07 21:14
 */
@Configuration
public class MyRabbitConfig {


    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * JSON 序列化对象配置
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitTemplate
     */
    @PostConstruct  //MyRabbitConfig对象创建完成以后执行这个方法
    public void initRabbitTemplate() {
        //服务器收到消息回调 ConfirmCallback
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             * 1、只要消息抵达服务器 ack=true
             * @param correlationData 当前消息唯一关联的数据 这个是消息爱的唯一id
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //服务器收到了
                System.out.println("confirmCallback："+correlationData+"--ack："+ack+"--cause："+cause);

            }
        });

        //消息抵达队列回调 returnCallback
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            //消息没有投递给指定的队列，会触发这个回调

            /**
             *
             * @param message 投递失败的消息详细信息
             * @param replyCode 恢复的状态码
             * @param replyText 恢复的文本内容
             * @param exchange 当时发送给的交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("returnCallback: Fail Message["+message+"]"
                        +"--code"+replyCode
                        +"--replyText:"+replyText
                        +"---exchange:"+exchange
                        +"--routingKey"+routingKey);
            }
        });

        //消费端确认
            //*默认是自动确认的， 只要消息接收到，客户端会自动确认，服务端就会移除这个消息
            //
    }
}
