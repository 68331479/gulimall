package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: LDeng
 * @Date: 2020-12-14 9:42
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index","/index.html"})
    public String indexPage(Model model){
        //TODO 1， 查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        //视图解析器拼串
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    //index/catalog.json
    @ResponseBody//以json的形式返回， 所以加ResponseBody, 而不是跳转页面
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJSon();
        System.out.println(map);
        return map;
    }

    //测试性能 简单服务
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        //1，获取一把锁， 只要锁名字一样就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        //2，加锁
        lock.lock();//阻塞式等待，默认加的锁是30秒时间
        //1）锁自动续期，如果业务超长，运行期间自动给锁续上新的30s， 不用担心业务时间过长，锁自动过期被删掉
        //2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s后自动删除
        //lock.lock(10, TimeUnit.SECONDS);//10秒自动解锁
        try{
            System.out.println("加锁成功，执行业务=====>"+Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e){

        }finally {
            //3，解锁
            System.out.println("释放锁=====>"+Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    //保证一定能读到最新数据
    //修改期间，写锁是排他锁，读锁是共享锁
    //写锁没释放之前，读取操作必须等待

    //读+读 ：并发读，智慧在redis中记录好，会同时加锁成功
    //写+读  ： 等待写锁释放
    //写+写 ： 阻塞
    //读+写 ：有读锁 写也需要等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s="";
        try {
            //1 改数据加写锁
            rLock.lock();
            System.out.println("写锁加锁成功---------"+Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
             rLock.unlock();
            System.out.println("写锁释放---------"+Thread.currentThread().getId());
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s="";
        try {
            //加读锁
            rLock.lock();
            System.out.println("读锁加锁成功---------"+Thread.currentThread().getId());
            Thread.sleep(30000);
            s = redisTemplate.opsForValue().get("writeValue");
        }catch (Exception e){
        }finally {
            rLock.unlock();
            System.out.println("读锁释放---------"+Thread.currentThread().getId());
        }
        return s;
    }




}
