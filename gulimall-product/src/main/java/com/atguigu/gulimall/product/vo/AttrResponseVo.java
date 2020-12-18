package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2020-11-27 16:55
 */
@Data
public class AttrResponseVo extends AttrVo {
    /**
     "catelogName": "手机/数码/手机", //所属分类名字
     "groupName": "主体", //所属分组名字
    */
     private String catelogName;

     private String groupName;

     private Long[] catelogPath;


}
