package com.atguigu.gulimall.cart.service.impl;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:40
 */

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    private final String CART_PREFIX="gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        // 得到用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey="";//redis的key格式
        //使用userInfoTo里面是否有userID,判断是否是登录用户
        if(userInfoTo.getUserId()!=null){
            //登录状态  key: gulimall:cart:1
            cartKey=CART_PREFIX+userInfoTo.getUserId();
        }else{
            //没登录状态
            cartKey=CART_PREFIX+userInfoTo.getUserKey();
        }

        return null;
    }
}
