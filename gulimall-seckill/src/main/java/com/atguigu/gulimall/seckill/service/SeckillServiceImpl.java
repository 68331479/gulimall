package com.atguigu.gulimall.seckill.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkusVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: LDeng
 * @Date: 2021-01-18 14:46
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX= "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX="seckill:skus";

    //sku库存信号量
    private final String SKU_STOCK_SEMAPHORE="seckill:stock:";//+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
            //远程查询coupon模块获取最近三天需要参加秒杀的活动
        R latest3DaySession = couponFeignService.getLatest3DaySession();
        if(latest3DaySession.getCode()==0){
            //上架商品
            List<SeckillSessionsWithSkusVo> seckillSessionsWithSkus =
                    latest3DaySession.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            //缓存到redis
                //1, 缓存活动信息
                    saveSessionInfosToRedis(seckillSessionsWithSkus);
                //2, 缓存活动商品信息
                    saveSessionSkuInfosToRedis(seckillSessionsWithSkus);

        }
    }

    private void saveSessionInfosToRedis(List<SeckillSessionsWithSkusVo> sessions){
        sessions.stream().forEach(session->{
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key=SESSIONS_CACHE_PREFIX+startTime+"_"+endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if(!hasKey){
                List<String> skuIds =
                        session.getRelationSkus().stream()
                                .map(item->item.getPromotionSessionId().toString()+"_"+item.getSkuId().toString())
                                .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key,skuIds);
            }
        });
    }

    //获取缓存活动的商品信息（秒杀+基本）
    private void saveSessionSkuInfosToRedis(List<SeckillSessionsWithSkusVo> sessions){
        sessions.stream().forEach(session ->{
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo->{
                Boolean hasSkuInfoKey = hashOps.hasKey(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString());
                String token = UUID.randomUUID().toString().replace("-", "");
                if(!hasSkuInfoKey){
                    //缓存商品(sku基本信息和秒杀信息)
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    //1, sku的基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if(skuInfo.getCode()==0){
                        SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(skuInfoVo);
                    }
                    //2， sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);
                    //3, 设置当前商品的时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //4, 随机码 防刷
                    redisTo.setRandomCode(token);
                    //存入redis
                    String jsonString = JSON.toJSONString(redisTo);
                    hashOps.put(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString(),jsonString);

                    //如果当前场次的商品的库存信息已经上架，就不需要上架
                    //5，使用库存作为分布式信号量  限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }

            });
        } );
    }

    //查询当前时间参与秒杀的商品信息
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //1 确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        List<String> values=new ArrayList<>();
        for (String key : keys) {
            //seckill:sessions:1611028800000_1611122400000
            String[] split = key.replace("seckill:sessions:", "").split("_");
            Long start=Long.parseLong(split[0]);
            Long end=Long.parseLong(split[1]);
            if(time>=start && time<=end){
                //2 获取这个秒杀场次需要的所有商品信息
//                String s = redisTemplate.opsForList().leftPop(key);
//                values.add(s);
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if(list!=null && list.size()>0 ){
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo redisTo = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    //获取某个SKU商品的秒杀信息
    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if(keys!=null && keys.size()>0){
            String regx="\\d_"+skuId;
            for (String key : keys) {
                //正则判断
                boolean matches = Pattern.matches(regx, key);
                if(matches){
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                    //随机码
                    long currentTime = new Date().getTime();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if(currentTime>=startTime&&currentTime<=endTime){
                    }else{
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        long s0 = System.currentTimeMillis();
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        //1， 获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if(StringUtils.isEmpty(json)){
            return null;
        }else{
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //校验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long currentTime = new Date().getTime();
            long ttl = endTime - currentTime;
            System.out.println(ttl);
            if(currentTime>=startTime && currentTime<=endTime){//1，校验时间合法性
                //2， 校验随机码和商品id
                String redisrandomCode = redisTo.getRandomCode();
                String  rediskillId= redisTo.getPromotionId()+"_"+redisTo.getSkuId();
                if(redisrandomCode.equals(key) && rediskillId.equals(killId)){
                    //3, 校验购物数量是否合理
                     if(num<=redisTo.getSeckillLimit()){
                         //4, 验证此人是否已经购买过， 防刷， 幂等性处理
                            //只要秒杀成功， 就去redis中占位， userId_sessionId_skuId
                            //自动过期
                         String redisKey=respVo.getId()+"_"
                                 +redisTo.getPromotionSessionId()+"_"
                                 +redisTo.getSkuId();
                         Boolean aBoolean =
                                 redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl,TimeUnit.MILLISECONDS);
                         if(aBoolean){
                             //占位成功， 没买过
                             RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisrandomCode);
                                 boolean b = semaphore.tryAcquire(num);
                                 if(b){
                                     //秒杀成功，
                                     //快速下单， 发消息给MQ
                                     String timeId = IdWorker.getTimeId();
                                     SeckillOrderTo orderTo = new SeckillOrderTo();
                                     orderTo.setOrderSn(timeId);
                                     orderTo.setMemberId(respVo.getId());
                                     orderTo.setNum(num);
                                     orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                     orderTo.setSkuId(redisTo.getSkuId());
                                     orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                     rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                     long s1 = System.currentTimeMillis();
                                     log.info("秒杀消耗的时间：(毫秒) "+(s1-s0));
                                     return timeId;
                                 }else{
                                     return null;
                                 }
                         }else {
                             //占位失败，已经购买过
                             return null;
                         }

                     }else{
                         return null;
                     }
                }else{
                    return null;
                }
            }else {
                return null;
            }
        }
    }
}
