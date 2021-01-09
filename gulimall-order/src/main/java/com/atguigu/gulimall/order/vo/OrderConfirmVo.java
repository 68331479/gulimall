package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 15:47
 */

//订单确认页需要的数据
@Data
public class OrderConfirmVo {

    //收货地址列表  ums->member_receive_address
    List<MemberAddressVo> address;


    //所有选中的购物项
    List<OrderItemVo> items;

    //发票信息。。。

    //优惠券信息。。。 暂时用会员积分去计算 private Integer integration;
    Integer integration;

    BigDecimal total;//订单总额

    BigDecimal payPrice;//应付价格

}
