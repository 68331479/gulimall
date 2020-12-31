package com.atguigu.gulimall.auth.controller;

/**
 * @Author: LDeng
 * @Date: 2020-12-31 14:50
 */


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.MemberRespVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    //社交登录成功后的回调
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code,HttpSession session) throws Exception {
        //1, 根据code换取AccessToken
        Map<String, String> map = new HashMap<>();
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        map.put("client_id", "2628561067");
        map.put("client_secret", "c4bbf71d3516574950e473e66aa1e4ed");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.
                doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);

        if (response.getStatusLine().getStatusCode() == 200) {
            //获取accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //得到社交用户信息
            //1，如果是第一次使用， 为当前社交用户生成一个会员信息账号， 以后这个社交账号就对应制定的会员
            //登录或者注册这个社交用户
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode() == 0) {
                MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                System.out.println("登录成功：用户信息为："+ data);
                //子域之间  gulimall.com auth.gulimall.com order.gulimall.com
                //发Session的时候， 即使是子域系统发的，也能让父域使用， 方法是是指定父域名
                session.setAttribute("loginUser",data);
                // 登录成功跳转回首页
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
