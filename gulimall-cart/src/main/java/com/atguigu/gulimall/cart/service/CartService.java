package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
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

    //获取整个购物车
    Cart getCart() throws ExecutionException, InterruptedException;

    //清空购物车
    void clearCart(String cartKey);

    //勾选购物项
    void checkItem(Long skuId, Integer check);

    //修改购物项数量
    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);


    List<CartItem> getUserCartItems();
}
