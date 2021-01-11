package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal=new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //登录拦截器里面获取会员Id
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1,远程查询会员地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> getCartFuture = CompletableFuture.runAsync(() -> {
            //2, 远程查询购物车所有选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            //Feign在做远程调用的时候， 没有请求头， 所以带不了cookie， 导致远程调用不知到是哪个用户， 返回空
            //解决方法： 加上feign远程调用的请求拦截器
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            List<SkuHasStockVo> data = wmsFeignService.getSkusHasStock(collect);
            if(data!=null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        },executor);

        //3, 查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);
        //4, 其他数据在VO中自动计算
        //TODO: 5, 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //向redis存一份令牌
        redisTemplate.opsForValue()
                .set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),
                        token,30, TimeUnit.MINUTES);
        //给页面存一份
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddressFuture,getCartFuture).get();

        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo response=new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //去创建订单， 验令牌，验价格，锁库存。。。
        //验证令牌必须保证是原子性的， 使用鲁尔脚本
        String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //原子严重令牌和删除令牌， 返回0或者1
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);
        if(result==0L){
            //验证失败
            response.setCode(1);
            return  response;
        }else{
            //验证成功, 进入下单逻辑
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){//价差小于0.01，对比成功

            }else{//对比失败
                response.setCode(2);
                return response;
            }
        }
        return response;
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo createTo = new OrderCreateTo();

        //1, 生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //2， 获取到所有订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //3, 计算价格,积分等信息
        computePrice(orderEntity,orderItemEntities);

        return createTo;

    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //订单价格相关
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion =new BigDecimal("0.0");
        BigDecimal gift =new BigDecimal("0.0");
        BigDecimal growth =new BigDecimal("0.0");
        for (OrderItemEntity entity : orderItemEntities) {
            //订单总额叠加每个订单项的总额
            total = total.add(entity.getRealAmount());
            coupon = coupon.add(entity.getCouponAmount());
            integration=integration.add(entity.getIntegrationAmount());
            promotion=promotion.add(entity.getPromotionAmount());

            gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth.add(new BigDecimal(entity.getGiftGrowth().toString()));

        }
        //订单总额
        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        //设置应付总额（订单总额+运费）
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        //积分，成长值
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        //订单删除状态
        orderEntity.setDeleteStatus(0);//未删除



    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);

        //2，远程获取收货地址信息,运费
        R fare = wmsFeignService.getFare(confirmVoThreadLocal.get().getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //3，设置运费信息
        entity.setFreightAmount(fareResp.getFare());
        //4，设置收货人信息
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());
        entity.setReceiverCity(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());

        //设置订单的状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);//默认7天自动收货

        return entity;
    }

    /**
     * 构建所有订单项数据
     * @param
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems!=null && currentUserCartItems.size()>0){
            List<OrderItemEntity> orderItemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderitemEntity = buildOrderItem(cartItem);
                orderitemEntity.setOrderSn(orderSn);
                return orderitemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }
        return null;
    }

    /**
     * 根据购物车条目创建订单项条目
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        Long skuId = cartItem.getSkuId();
        //1，订单信息： 订单号
        //2，商品的spu信息
        R data = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = data.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //3，商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4，优惠信息（暂忽略）
        //5, 积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        //6, 订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        //当前订单项的实际金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }








}