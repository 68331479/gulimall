package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:39
 */
public interface CartService {
    //将skuId商品添加到购物车
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    //获取购物车中的skuId购物项
    CartItem getCartItem(Long skuId);
}
