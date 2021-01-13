package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-10 15:39
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasstock")
    List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLock(@RequestBody WareSkuLockVo vo);

}
