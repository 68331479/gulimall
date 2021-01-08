package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {


    /**
     * 1， 创建exchange[hello-java-exchange]，Queue ,Binding
     *      1） 使用AmqpAdmin创建
     * 2，如何收发消息
     */
    @Autowired
    AmqpAdmin amqpAdmin;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest(){
        //1, 测试发送消息
        String msg="1111111111111";
        //2, 测试发送对象
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("删库跑路！！！");
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",reasonEntity);
        log.info("消息发送完成{}",reasonEntity);
    }



    @Test
    void createExchange() {
        //public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功："+"hello-java-exchange");
    }

    @Test
    public void createQueue(){
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功","hello-java-queue");
    }

    @Test
    public void createBinding(){
        //public Binding(String destination, 目的地
        //              Binding.DestinationType destinationType, 目的地类型
        //              String exchange, 交换机
        //              String routingKey, 路由键
        //              @Nullable Map<String, Object> arguments) {} 参数
        Binding binding = new Binding("hello-java-queue",
                                        Binding.DestinationType.QUEUE,
                                        "hello-java-exchange",
                                        "hello.java",
                                        null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功","hello-java-binding");
    }

}
