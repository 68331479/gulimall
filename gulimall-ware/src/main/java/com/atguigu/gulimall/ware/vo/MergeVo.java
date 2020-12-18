package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-04 16:27
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
