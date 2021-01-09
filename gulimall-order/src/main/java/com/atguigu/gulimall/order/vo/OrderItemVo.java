package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 15:57
 */

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;//属性组合
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;// 计算得来， 所以不用@Data 注解

}
