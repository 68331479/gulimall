package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-25 17:56
 */

@Data
public class SpuItemAttrGroupAttrVo {

    private String groupName;
    private List<SpuBaseAttrVo> attrs;

}
