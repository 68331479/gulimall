package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: LDeng
 * @Date: 2020-12-28 17:08
 */

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer{

    //视图映射
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /**
         *    @GetMapping("/login.html")
                public String loginPage(){

                return "login";
                }
         */

        registry.addViewController("/reg.html").setViewName("reg");
    }
}
