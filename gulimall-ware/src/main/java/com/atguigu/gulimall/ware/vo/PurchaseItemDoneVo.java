package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2020-12-07 11:18
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;
    private Integer status;
    private String reason;

}
