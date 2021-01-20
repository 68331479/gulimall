package com.atguigu.gulimall.seckill.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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

}
