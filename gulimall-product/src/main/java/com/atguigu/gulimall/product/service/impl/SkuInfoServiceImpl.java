package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         key:
         catelogId: 0
         brandId: 0
         min: 0
         max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {//和0比较
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SkuInfoEntity> skuInfoEntities = this.list(queryWrapper);
        return skuInfoEntities;
    }


    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1 , sku基本信息获取pms_sku_info
            SkuInfoEntity info = baseMapper.selectById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);
        /**
         * 3，4，5 依赖第一步的执行结果
         */
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3 , spu销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4 , spu 的属性 pms_spu_info_desc  图片介绍
            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfo);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5 , spu规格参数信息, 属性组（包含具体属性）
            List<SpuItemAttrGroupAttrVo> attrGroupAttrVos
                    = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getSkuId());
            skuItemVo.setGroupAttrs(attrGroupAttrVos);
        }, executor);


        /**
         * 与第一步的结果无关， 直接执行
         */
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //2 , sku图片信息 pms_sku_images
            List<SkuImagesEntity> images =
                    skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        //查询商品的秒杀信息
        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
            R seckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (seckillInfo.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = seckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        }, executor);

        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imagesFuture,secKillFuture).get();


        return skuItemVo;
    }
}