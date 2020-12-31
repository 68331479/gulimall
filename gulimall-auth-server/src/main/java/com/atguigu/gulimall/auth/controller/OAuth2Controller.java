package com.atguigu.gulimall.auth.controller;

/**
 * @Author: LDeng
 * @Date: 2020-12-31 14:50
 */


import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        //1, 根据code换取AccessToken
        Map<String,String> map = new HashMap<>();
        Map<String,String> header=new HashMap<>();
        Map<String,String> query=new HashMap<>();
        map.put("client_id","2628561067");
        map.put("client_secret","c4bbf71d3516574950e473e66aa1e4ed");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.
                doPost("https://api.weibo.com", "/oauth2/access_token", "post", header,query,map);

        if(response.getStatusLine().getStatusCode()==200){
            //获取accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //得到社交用户信息
            //1，如果是第一次使用， 为当前社交用户生成一个会员信息账号， 以后这个社交账号就对应制定的会员
            //登录或者注册这个社交用户





        }else {
            return "redirect:http://auth.gulimall.com/login.html";
        };
        //2， 登录成功跳转回首页
        return "redirect:http://gulimall.com";
    }
}
