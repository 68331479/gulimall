package com.atguigu.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 15:47
 */

//订单确认页需要的数据

public class OrderConfirmVo {

    //收货地址列表  ums->member_receive_address
    @Getter @Setter
    List<MemberAddressVo> address;


    //所有选中的购物项
    @Getter @Setter
    List<OrderItemVo> items;

    //发票信息。。。

    //优惠券信息。。。 暂时用会员积分去计算 private Integer integration;
    @Getter @Setter
    Integer integration;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    //TODO:防重复提交令牌
    @Getter @Setter
    String orderToken;

    public Integer getCount(){
        Integer i=0;
        if(items!=null){
            for (OrderItemVo item : items) {
               i+= item.getCount();
            }
        }
        return i;
    }


   // BigDecimal total;//订单总额

    public BigDecimal getTotal() {
        BigDecimal sum=new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum=sum.add(multiply);
            }
        }
        return sum;
    }

   // BigDecimal payPrice;//应付价格

    public BigDecimal getPayPrice() {
      return getTotal();
    }
}
