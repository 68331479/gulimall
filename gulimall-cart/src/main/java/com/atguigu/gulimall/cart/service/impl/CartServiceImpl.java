package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
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
import java.util.stream.Collectors;

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
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String data =(String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(data, CartItem.class);
        return cartItem;
    }

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

    //获取整个购物车
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart =new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            //1, 用户是登录状态
            String cartKey =CART_PREFIX+ userInfoTo.getUserId();
            //1.1, 合并临时购物车的数据
            String tempCartKey=CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if(tempCartItems!=null){
                //临时购物车有数据，需要合并；
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //清除临时购物车
                clearCart(tempCartKey);
            }
            //1.2, 获取登录后的购物车的数据[包含合并过来的临时购物车条目]
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            //2， 没登录
            String cartKey =CART_PREFIX+ userInfoTo.getUserKey();
            //获取临时购物的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
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

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if(values!=null && values.size()>0){
            List<CartItem> collect = values.stream().map((obj) -> {
                String str=(String)obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        //直接删键，
        redisTemplate.delete(cartKey);

    }
}
