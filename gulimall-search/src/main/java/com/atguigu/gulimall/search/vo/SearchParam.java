package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-19 13:24
 */
/**
 * 封装检索条件
 * catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0&brandId=1&brandId=2..
 *
 */

@Data
public class SearchParam {

    private String keyword;//页面传递过来的全文匹配关键字

    private Long catalog3Id;//三级分类ID

    /*
        sort=saleCount_asc/desc
        sort=skuPrice_acs/desc
        sort=hotScore_asc/desc
     */
    private String sort;//排序条件

    /*
        过滤条件
        hasStock 是否有货
        skuPrice区间 ，1_500/_500/500_
        brandId
        attrs attr=1_其他：安卓  冒号分割&attrs=2_5:6
     */
    private Integer hasStock;//是否显示有货， 页面传递来的是0， 1
    private String skuPrice;//价格区间查询
    private List<Long> brandId;//品牌筛选,, 支持多选
    private List<String> attrs;//按照属性进行筛选

    private Integer pageNumber=1;//页码， 默认第一页

    private String _queryString;//原生的所有查询条件


}
