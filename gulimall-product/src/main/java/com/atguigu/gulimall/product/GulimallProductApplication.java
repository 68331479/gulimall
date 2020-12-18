package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
 * 1, 整合mybatis plus
 *  1) 导入依赖
 *  2）配置
 *      1， 配置数据源
 *          1 >引入mysql驱动
 *          2 > application.yml配置数据源
 *      2，配置mybatis-plus
 *          1>@MapperScan("com.atguigu.gulimall.product.dao")
 *          2>配置mybatis-plus的mapper.xml
 *
 */

@EnableCaching
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.product.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages ="com.atguigu.gulimall.product.feign")
@ComponentScan(basePackages = {"com.atguigu"})
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
