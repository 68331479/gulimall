package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: LDeng
 * @Date: 2021-01-20 10:42
 */
@FeignClient(value="gulimall-seckill",fallback = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    @GetMapping("/sku/skuseckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);


}
