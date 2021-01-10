package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 23:30
 */

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1, 通过RequestContextHolder拿到请求数据， 即/toTrade请求的request(包含cookie信息)
                ServletRequestAttributes requestAttributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes!=null){
                    HttpServletRequest request = requestAttributes.getRequest();//老请求
                    if(request!=null){
                        //同步请求头数据， 主要是cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求设置cookie
                        requestTemplate.header("Cookie",cookie);
                    }
                }
            }
        };
    }
}
