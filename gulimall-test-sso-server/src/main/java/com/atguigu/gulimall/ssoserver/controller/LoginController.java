package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 13:34
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String url,
                          HttpServletResponse response){

        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){
            //登录成功 跳转, 跳回到之前的页面
           //将用户信息存入redis
            String uuid= UUID.randomUUID().toString().replace("-","");
            redisTemplate.opsForValue().set(uuid,username);
            Cookie sso_token = new Cookie("sso_token",uuid);
            response.addCookie(sso_token);
            return "redirect:"+url+"?token="+uuid;
        }
        //登录失败，展示登录页；
        return "login";
    }


    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,
                            Model model,
                            @CookieValue(value="sso_token",required = false) String sso_token ){
        if(!StringUtils.isEmpty(sso_token)){
            //说明之前登录过， 浏览器留下了cookie
            return "redirect:"+url+"?token="+sso_token;
        }

        //获取url 放入到model， /doLogin的时候可以获取
        model.addAttribute("url",url);
        return "login";
    }



}
