package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Author: LDeng
 * @Date: 2021-01-17 11:17
 */

@FeignClient("gulimall-member")
public interface OrderFeignService {

    @GetMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
