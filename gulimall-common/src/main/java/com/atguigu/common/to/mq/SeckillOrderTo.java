package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-20 15:51
 */
@Data
public class SeckillOrderTo {
    private String orderSn;//订单号
    private Long promotionSessionId;//场次id
    private Long skuId;//商品id
    private BigDecimal seckillPrice;//秒杀价格
    private Integer num;//秒杀的数量
    private Long memberId;//会员id
}
