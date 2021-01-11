package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-11 12:58
 */

@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
