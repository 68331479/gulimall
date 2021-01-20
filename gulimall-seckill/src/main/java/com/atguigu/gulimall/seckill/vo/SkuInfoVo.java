package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 10:31
 */
@Data
public class SkuInfoVo {
    private Long skuId;
    private Long spuId;
    private String skuName;
    private String skuDesc;
    private Long catalogId;
    private Long brandId;
    private String skuDefaultImg;
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;
    private Long saleCount;

}
