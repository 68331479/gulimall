package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;





    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //5.4 sku的优惠，满减等信息 gulimall_sms
        // >sms_sku_ladder 几件几折
        Long skuId = skuReductionTo.getSkuId();
        SkuLadderEntity skuLadderEntity=new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuId);
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());//满几件
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());//几折
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());//是否叠加其他优惠
        if(skuReductionTo.getFullCount()>0){//有打折信息再存 ， 去除空记录
            skuLadderService.save(skuLadderEntity);
        }

        // >sms_sku_full_reduction 满减
        SkuFullReductionEntity skuFullReductionEntity=new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        if(skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1){
            this.save(skuFullReductionEntity);//有满减才存
        }

        //todo add-other字段

        //>sms_member_price 会员价格
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream()
                .map((item) -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(skuId);
                    memberPriceEntity.setMemberLevelId(item.getId());//memberPrice的id和这里的memberLevelId对应
                    memberPriceEntity.setMemberLevelName(item.getName());
                    memberPriceEntity.setMemberPrice(item.getPrice());
                    memberPriceEntity.setAddOther(1);//默认叠加其他优惠
                    return memberPriceEntity;
                }).filter((item)->{
                    return item.getMemberPrice().compareTo(new BigDecimal("0"))==1;//memberprice >0才保存
                }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }
}