package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-11 14:09
 */

@FeignClient("gulimall-ware")
public interface WareFeignService {

    //查询sku是否有库存
    @PostMapping("/ware/waresku/hasstock")
    List<SkuHasStockVo>  getSkusHasStock(@RequestBody List<Long> skuIds);

}
