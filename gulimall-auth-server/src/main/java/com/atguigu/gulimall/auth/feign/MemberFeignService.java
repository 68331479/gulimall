package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
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
    public R regist(@RequestBody UserRegisteVo vo);

}
