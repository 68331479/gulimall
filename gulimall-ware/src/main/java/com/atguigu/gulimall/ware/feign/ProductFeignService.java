package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: LDeng
 * @Date: 2020-12-07 12:33
 */

@FeignClient("gulimall-product")
public interface ProductFeignService {

    //product/skuinfo/info/{skuId}
    //api/product/skuinfo/info/{skuId}
    //两种写法都可以， 带api的要经过网关， 上面的注解要变成gulimall-gateway

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
