package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * @Author: LDeng
 * @Date: 2021-01-18 13:13
 */
@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class HelloScheduled {

    /**
     * 1, spring 的cron 只允许6位，
     * 2, 定时任务默认是阻塞的
     *         1. 异步方式，自己提交到线程池执行
     *         2. 支持定时任务线程池 通过设置 spring.task.scheduling.pool.size=10
     *         3. 让定时任务异步执行
     *  最终解决：
     *      使用异步+定时任务来完成定时任务不阻塞
     */
    @Async
    //@Scheduled(cron = "* * * * * ?")
    public void hello() throws InterruptedException {
      log.info("hello....");
      Thread.sleep(6000);
    }
}
