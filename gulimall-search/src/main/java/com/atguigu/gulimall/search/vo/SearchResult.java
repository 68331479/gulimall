package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-19 14:07
 */

@Data
public class SearchResult {
    //商品信息
    private List<SkuEsModel> products;

    //分页信息
    private Integer pageNumber;//当前页面

    private Long total; //总记录数

    private Integer totalPages;//总页码

    private List<Integer> pageNavs;

    private List<BrandVo> brands;//当前查询结果所有涉及到的品牌

    private List<CatalogVo> catalogs;//当前查询结果所有涉及到的三级分类

    private List<AttrVo> attrs;//当前查询到的结果所有涉及到的属性


    //===================以上是返回给页面的所有信息=========================================

    //面包屑导航数据
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;//跳转地址
    }


    //内部类
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
