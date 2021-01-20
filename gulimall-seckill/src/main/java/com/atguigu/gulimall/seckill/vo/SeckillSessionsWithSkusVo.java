package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 9:22
 */

@Data
public class SeckillSessionsWithSkusVo {

    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Date createTime;

    private List<SeckillSkuVo> relationSkus;
}
