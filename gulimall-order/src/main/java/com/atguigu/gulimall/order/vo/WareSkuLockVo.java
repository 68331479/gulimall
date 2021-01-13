package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-12 8:50
 */
@Data
public class WareSkuLockVo {

    private String orderSn;//订单号

    private List<OrderItemVo> locks;//需要锁住的所有库存信息

}
