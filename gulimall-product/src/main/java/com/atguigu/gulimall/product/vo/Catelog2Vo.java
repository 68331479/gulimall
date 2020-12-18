package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-14 15:31
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {

    private String catalog1Id;//一级父分类id
    private List<Catelog3Vo> catalog3List; //三级子分类
    private String id;//二级分类id
    private String name;//二级分类名称

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo{ //内部类，三级分类
        private String catalog2Id;//父分类，二级分类id
        private String id;//三级分类id
        private String name;//三级分类名称
    }

}
