package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 9:24
 */

@Data
public class SeckillSkuVo {

    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;
}
