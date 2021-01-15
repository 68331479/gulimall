package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Transactional
    @Override//重载一个解锁库存的方法
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询最新的库存解锁状态，防止重复解锁
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = taskEntity.getId();
        //按照库存工作单，找到所有工作单明细
        List<WareOrderTaskDetailEntity> taskDetailEntities =
                wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskId)
                        .eq("lock_status",1));
        for (WareOrderTaskDetailEntity taskDetailEntity : taskDetailEntities) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }

    @Override
    public void unLockStock(StockLockedTo to) {
        //解锁
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //由于部分商品圈库存失败导致的需要解锁库存， 所以需要判断一下detailId;
        //有记录：表示是下单业务失败导致的解锁需求，需要解锁
        //没有记录：表示是锁库存本身导致的失败，库存会自动回滚， 无需解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            //没有订单：==>订单业务回滚导致的， 需要解锁
            //有订单：
            //===> 订单状态已取消： 解锁库存
            //===> 只要没取消， 不能解锁；
            Long id = to.getId();
            WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getById(id);
            String orderSn = orderTaskEntity.getOrderSn();
            //根据订单号查询订单状态,远程调用order
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {//订单返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                //订单下单失败，解锁库存
                //订单是取消状态，解锁库存
                if (data == null || data.getStatus() == 4) {
                    if (byId.getLockStatus() == 1) {//order_task_detail的状态是1的时候才解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝以后，重新放到队列，让别人继续消费解锁；
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁
        }
    }

    /**
     * 库存自动解锁
     * 2） 下单成功，库存锁定成功， 接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁
     */


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        baseMapper.unlock(skuId, wareId, num);
        //更新库存工作单的状态为2-已解锁
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变成已解锁
        wareOrderTaskDetailService.updateById(entity);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         skuId:
         wareId:
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1, 判断是否有库存记录
        List<WareSkuEntity> entities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {//之前没有库存记录
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO:远程查询skuname ,如果失败 事务不回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
            }
            baseMapper.insert(wareSkuEntity);

        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查询当前sku的总库存量， 包含所有仓库的， 并且减掉已经锁定的库存
            //SELECT SUM(stock-stock_locked) FROM `wms_ware_sku` WHERE sku_id=1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 库存解锁的场景
     * 1） 下单成功， 订单过期没有支付，被系统自动取消， 被用户手动取消， 都要解锁库存
     * 2） 下单成功，库存锁定成功， 接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁
     *
     * @param vo
     * @return
     */
    @Transactional//(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //0， 保存库存工作单详情， 追溯
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);


        //1， 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = baseMapper.listWareIdhasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        //2，锁定库存

        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有仓库有商品
                throw new NoStockException(skuId);
            }
            //1，如果每个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            //2， 如果锁定失败，前面保存的工作单信息就会回滚， 发送出去的消息， 即使要解锁库存也会找不到记录而解锁不了
            for (Long wareId : wareIds) {
                //成功就返回1， 否则就是0, 返回的是受影响的行数， 所以是1或者0
                Long count =
                        baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    //库存锁定成功，跳出循环
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity =
                            new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(),
                                    taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    //这里需要发实体类， 而不是id， 因为如果前面回滚了，再拿id去表中wms_ware_order_taskdetail会查不到记录；
                    lockedTo.setDetail(stockDetailTo);
                    //发送消息 告诉MQ 库存锁定成功
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //当前仓库锁定失败，重试下一个仓库

                }
            }
            //能运行到这里， 所有库存都是锁定成的
            if (skuStocked == false) {
                //当前商品所有仓库都没锁到库存
                throw new NoStockException(skuId);
            }
        }

        //全部锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}