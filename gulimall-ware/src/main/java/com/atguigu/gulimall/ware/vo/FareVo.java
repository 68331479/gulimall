package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: LDeng
 * @Date: 2021-01-10 17:46
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
