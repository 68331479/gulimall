package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-11 10:56
 */

//封装订单提交的数据
@Data
public class OrderSubmitVo {

    private Long addrId;//收货地址id
    private Integer payType;//支付方式
    //无需提交需要购买的商品， 去购物车再获取一遍勾选的item
    //优惠，发票
    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格，验价

    private String note;
    //用户相关信息都在session里面


}
