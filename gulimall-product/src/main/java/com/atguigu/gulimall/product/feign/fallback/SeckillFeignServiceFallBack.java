package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: LDeng
 * @Date: 2021-01-22 10:34
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("远程调用seckillFeignService失败");
        return R.error(10003, "远程调用seckillFeignService失败");
    }
}
