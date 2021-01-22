package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: LDeng
 * @Date: 2021-01-19 14:09
 */
@Slf4j
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;
    /**
     * 返回当前时间可以参与的秒杀商品信息,,
     * @return
     */
    @ResponseBody
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus(){
        log.info("=======>测试sentinel有没有调用方法<======");
        List<SecKillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(tos);
    }

    @ResponseBody
    @GetMapping("/sku/skuseckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SecKillSkuRedisTo redisTo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(redisTo);
    }


    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num, Model model){
        // 判断是否登录,interceptor 完成
        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
       return "success";
    }
}
