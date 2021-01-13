package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2021-01-12 8:55
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private boolean locked;

}
