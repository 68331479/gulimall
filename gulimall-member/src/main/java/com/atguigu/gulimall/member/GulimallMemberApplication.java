package com.atguigu.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 *   1， 想要调用gulimall-coupon服务
 *      1.1 引入open-feign
 *      1.2 编写一个接口， 告诉SpringCloud这个接口需要调用远程服务，都放在feign包下
 *              声明接口的每个方法都是调用远程服务的哪个请求
 *      1.3 开启远程调用功能 @EnableFeignClients
 *
 */
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.member.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gulimall.member.feign")
@ComponentScan(basePackages = {"com.atguigu"})
@EnableRedisHttpSession
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
