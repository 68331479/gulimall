package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-07 11:18
 */
@Data
public class PurchaseDoneVo {

    private Long id;
    private List<PurchaseItemDoneVo> items;
}
