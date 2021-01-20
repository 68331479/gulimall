package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock";

    @Scheduled(cron = "*/15 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        //1， 重复上架无需处理
        log.info("上架秒杀的商品信息。。。。。。");
        //分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }

}
