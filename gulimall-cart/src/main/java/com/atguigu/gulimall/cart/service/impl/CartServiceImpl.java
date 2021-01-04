package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: LDeng
 * @Date: 2021-01-02 23:40
 */

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX="gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res =(String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            //Redis购物车中购物车无此商品
            //添加新商品到购物车
            CartItem cartItem = new CartItem();
            //异步编排任务
            //1， 根据skuId,远程调用product服务查询当前要添加的商品信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(data.getSkuId());
                cartItem.setPrice(data.getPrice());
            }, executor);

            //2, 远程查询sku的销售属性信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            });

            CompletableFuture.allOf(getSkuInfoTask,getSkuSaleAttrValues).get();
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),jsonString);
            return cartItem;
        }
        else{
            //购物车有此商品， 修改数量即可
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),jsonString);//转成json放入redis
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String data =(String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(data, CartItem.class);
        return cartItem;
    }

    //获取到要操作的购物车
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // 得到用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey="";//redis的key格式
        //使用userInfoTo里面是否有userID,判断是否是登录用户
        if(userInfoTo.getUserId()!=null){
            //用户登录状态  key: gulimall:cart:1
            cartKey=CART_PREFIX+userInfoTo.getUserId();
        }else{
            //没登录状态 key:gulimall:cart:xxx(uuid)
            cartKey=CART_PREFIX+userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

}
