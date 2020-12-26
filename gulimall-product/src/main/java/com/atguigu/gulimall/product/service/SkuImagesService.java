package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author ldeng
 * @email 68331479@qq.com
 * @date 2020-11-11 17:14:39
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveImages(List<SkuImagesEntity> skuImagesEntities);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

