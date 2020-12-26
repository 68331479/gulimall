package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2020-12-25 14:48
 */

@Data
public class SkuItemVo {

    //1 , sku基本信息获取pms_sku_info
    SkuInfoEntity info;

    //有无货， 默认有
    boolean hasStock =true;

    //2 , sku图片信息 pms_sku_images
    List<SkuImagesEntity> images;

    //3 , sku->spu销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4 , spu 的属性 pms_spu_info_desc
    SpuInfoDescEntity desc;


    //5 , spu规格参数信息
    List<SpuItemAttrGroupAttrVo> groupAttrs;









}
