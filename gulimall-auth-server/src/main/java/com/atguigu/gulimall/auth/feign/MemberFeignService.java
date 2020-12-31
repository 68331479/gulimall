package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisteVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: LDeng
 * @Date: 2020-12-30 16:08
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/regist")
    R regist(@RequestBody UserRegisteVo vo);

    @PostMapping("member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;

}
