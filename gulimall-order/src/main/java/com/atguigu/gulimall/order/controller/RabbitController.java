package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: LDeng
 * @Date: 2021-01-07 22:41
 */

@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;


    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num",defaultValue = "100") Integer num){

        for(int i=0;i<num;i++){
            if(i%3==0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("删库跑路！！！"+i);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",reasonEntity);
            }else{
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderEntity);
            }
        }
        return "ok";
    }
}
