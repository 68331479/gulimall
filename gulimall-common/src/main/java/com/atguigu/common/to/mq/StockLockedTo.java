package com.atguigu.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: LDeng
 * @Date: 2021-01-14 10:36
 */

@Data
public class StockLockedTo implements Serializable{
    /**
     * 库存工作单的id wms_ware_order_task 的 id
     */
    private Long id;

    /**
     * 所有工作单详情
     */
    private StockDetailTo detail;

}
