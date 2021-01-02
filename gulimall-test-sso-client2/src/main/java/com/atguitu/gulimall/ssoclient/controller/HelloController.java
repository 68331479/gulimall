package com.atguitu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 13:09
 */
@Controller
public class HelloController {

    /**
     * 无需登录就可以访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){

        return "hello";
    }


    @Value("${sso.host.url}")
    String ssoServerUrl;

    @Autowired
    StringRedisTemplate redisTemplate;

    //能判断出是直接打开的还是从sso 登录成功后跳转回来的
    @GetMapping("/boss")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token",required = false) String token){

        if(!StringUtils.isEmpty(token)){
            //todo:拿着token 去redis获取用户名
            String username = redisTemplate.opsForValue().get(token);
            System.out.println("debug========>"+token);
            session.setAttribute("loginUser",username);
            model.addAttribute("loginUser",username);
        }

        String loginUser = (String)session.getAttribute("loginUser");
        if(loginUser==null){
            //没有登录， 跳转到登录服务器登录

            System.out.println(ssoServerUrl);
            return "redirect:"+ssoServerUrl+"?redirect_url=http://client2.com:8082/boss";
        }else{
            //登录了，可以取出信息
            List<String> emps=new ArrayList<>();
            emps.add("zs");
            emps.add("ls");
            model.addAttribute("emps",emps);
            return "list";
        }
    }




}
