package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 14:09
 */
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;
    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus(){

        List<SecKillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(tos);
    }

    @GetMapping("/sku/skuseckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SecKillSkuRedisTo redisTo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(redisTo);
    }

    @GetMapping("/kill")
    public R secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num){
        // 判断是否登录,interceptor 完成
        String orderSn = seckillService.kill(killId,key,num);
       return R.ok().setData(orderSn);
    }
}
