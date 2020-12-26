package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {


    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1));

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        //判断有没有purchaseId
        Long purchaseId=mergeVo.getPurchaseId();
        if(purchaseId==null){//新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //TODO 确认采购单的状态是新建或者是已分配（0，1）才允许合并
        List<Long> items = mergeVo.getItems();
        //更新wms_purchase_detail表
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map((item) -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity=new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


    @Override
    public void received(List<Long> ids) {//参数是采购单id
        //1,确认当前采购单是新建或者是已分配状态
            //TODO:判断采购的是否是该采购员的采购单
        List<PurchaseEntity> collect = ids.stream().map((id) -> {// 查询出采购单
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter((item)->{//排除不是新建或者已分配的采购单
           if(item.getStatus()== WareConstant.PurchaseEnum.CREATED.getCode() ||
                   item.getStatus()== WareConstant.PurchaseEnum.ASSIGNED.getCode()){
               return true;
           }else{
               return false;
           }
        }).map((item)->{
            item.setStatus(WareConstant.PurchaseEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //2，改变采购单的状态
        this.updateBatchById(collect);
        //3，改变采购项的状态
        collect.forEach((item)->{
            List<PurchaseDetailEntity> detailEntities
            = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntityUpdateStatusOnlys = detailEntities.stream().map((detailEntity) -> {
                PurchaseDetailEntity purchaseDetailEntityUpdateStatusOnly = new PurchaseDetailEntity();//只更新status，
                purchaseDetailEntityUpdateStatusOnly.setId(detailEntity.getId());
                purchaseDetailEntityUpdateStatusOnly.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return purchaseDetailEntityUpdateStatusOnly;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntityUpdateStatusOnlys);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        //1， 改变采购单中每条采购项状态
        Boolean flag=true;//采购单的标志位
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            if(item.getStatus()==WareConstant.PurchaseDetailEnum.HASERROR.getCode()){
                flag=false;
                detailEntity.setStatus(item.getStatus());
            }else{
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.FINISHED.getCode());
                //3，将成功采购的项入库 // 参数 ： 1， sku_id, ware_id, stock ， 哪个仓库，什么产品 入多少个
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        //2, 根据采购项判断采购单状态
        Long id = purchaseDoneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag==true?
                                    WareConstant.PurchaseEnum.FINISHED.getCode()
                                    :WareConstant.PurchaseEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }
}