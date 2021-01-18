package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @Author: LDeng
 * @Date: 2021-01-18 14:38
 */

//秒杀商品的定时上架
    //每天夜里3点， 上架最近三天需要秒杀的商品
        //当天 00：00：00-23：59：29
        //明天 00：00：00-23：59：29
        //后天 00：00：00-23：59：29
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days(){
        //1， 重复上架无需处理
        seckillService.uploadSeckillSkuLatest3Days();
    }

}
