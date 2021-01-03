package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:39
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num);
}
