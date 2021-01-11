package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2021-01-11 11:30
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    private Integer code;//0 成功， 其他。。

}
