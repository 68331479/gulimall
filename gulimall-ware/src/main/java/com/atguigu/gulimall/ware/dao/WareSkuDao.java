package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author ldeng
 * @email 68331479@qq.com
 * @date 2020-11-12 10:33:02
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId")Long wareId, @Param("skuNum") Integer skuNum);


    Long getSkuStock(@Param("skuId") Long skuId);
}
