package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 10:28
 */

@Data
public class SecKillSkuRedisTo {

    //sku秒杀 基本信息---SeckillSkuVo
    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;

    //sku详细信息---SkuInfoVo
    private SkuInfoVo skuInfoVo;

    //当前商品秒杀的开始时间和结束时间， Long类型便于比较
    private Long startTime;
    private Long endTime;

    //随机码，防刷
    private String randomCode;
}
