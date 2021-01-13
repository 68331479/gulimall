package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 13:15
 */

@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){

        return page;
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/testCreateOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setModifyTime(new Date());
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        //给MQ发送消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return "ok";
    }

}
