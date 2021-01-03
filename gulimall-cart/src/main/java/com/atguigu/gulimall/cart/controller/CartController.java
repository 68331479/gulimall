package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:48
 */
@Controller
public class CartController {

    /**
     * 浏览器有一个cookie: user-key,标识用户身份，一个月后过期
     * 如果第一次使用jd的购物车功能， 都会给一个临时的用户身份
     * 浏览器以后保存，每次访问都会带上这个cookie
     *
     * 登录：session
     * 没登录： 按照cookie里面带来的user-key来做
     * 第一次：如果没有临时用户，需要创建一个临时用户
     *
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(){

        //1,通过threadlocal得到用户信息id, user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        System.out.println(userInfoTo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(){

        return "success";
    }
}
