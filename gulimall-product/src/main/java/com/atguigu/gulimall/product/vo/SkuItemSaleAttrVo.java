package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-26 18:20
 */

@Data
public  class SkuItemSaleAttrVo{
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}

