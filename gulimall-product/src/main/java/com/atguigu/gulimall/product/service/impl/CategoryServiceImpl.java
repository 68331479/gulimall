package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;



//    @Caching(evict = {
//            @CacheEvict(value="category",key="'getLevel1Categorys'"),
//            @CacheEvict(value="category",key="'getCatalogJSon'")
//    })
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    @Override
    public void removeMenuByIds(List<Long> longs) {
        //TODO:1, 检查当前删除的菜单是否被其他地方引用
        //逻辑删除
        baseMapper.deleteBatchIds(longs);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        List<CategoryEntity> all = baseMapper.selectList(null);
        List<CategoryEntity> collect = all.stream()
                .filter(c -> c.getParentCid() == 0)
                .map(c -> {
                    c.setChildren(getChinldrens(c, all));
                    return c;
                })
                .sorted((a, b) -> ((a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort())))
                .collect(Collectors.toList());
        return collect;

    }

    public List<CategoryEntity> getChinldrens(CategoryEntity categoryEntity, List<CategoryEntity> all) {

        List<CategoryEntity> collect = all.stream()
                .filter(c -> c.getParentCid() == categoryEntity.getCatId())
                .map(c -> {
                    c.setChildren(getChinldrens(c, all));
                    return c;
                })
                .sorted((a, b) -> (a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort()))
                .collect(Collectors.toList());
        return collect;
    }

    @Cacheable(value = {"category"},key="#root.method.name")//每一个需要缓存的数据都需要指定要放到哪个名字的缓存（缓存分区， 推荐按照业务类型分）
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys()方法被执行了。。。。。");
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_cid", 0);
        List<CategoryEntity> categoryEntities = baseMapper.selectList(queryWrapper);
        return categoryEntities;
    }

    @Cacheable(value="category",key="#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJSon() {
        //查询出所有的数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //2, 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(//封装一级分类的map集合
                (k) -> (k.getCatId().toString()),
                v -> {
                    //查询每个一级分类的二级分类
                    List<CategoryEntity> level2Categorys = getParent_cid(selectList, v.getCatId());
                    //封装每个一级分类的二级分类
                    List<Catelog2Vo> catelog2Vos = null;
                    if (level2Categorys != null) {
                        catelog2Vos = level2Categorys.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                            //封装三级分类
                            List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());
                            if (level3Categorys != null) {
                                List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Categorys.stream().map(l3 -> {
                                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName().toString());
                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(catelog3Vos);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }
        ));
        return parent_cid;
    }

    //TODO 产生堆外内存溢出错误 OutofDirectMemoryError
   // @Override
    public Map<String, List<Catelog2Vo>> getCatalogJSon2() {
        /**
         * 1,缓存穿透， 缓存雪崩， 缓存击穿
         *  1.1 空结果缓存---> 缓存穿透
         *  1.2 设置随机过期时间---> 缓存雪崩
         *  1.3 加锁---> 缓存击穿
         */

        //1,加入缓存逻辑, 缓存中存的数据都是json字符串
        //JSON的好处可以跨语言， 跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {//缓存中没有
            //查询数据库
            System.out.println("缓存<<<<没有命中>>>>，查询数据库。。。。。。。");
            Map<String, List<Catelog2Vo>> catalogJSonFromDb = getCatalogJSonFromDbWithRedisLock();
            return catalogJSonFromDb;
        }
        System.out.println("缓存<<<<命中>>>>, Redis直接返回。。。。");
        //缓存中有： 解析JSON 到对象返回
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }


    //使用Redisson实现分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJSonFromDbWithRedissonLock() {

        //1, 占分布式锁, 注意这里锁的名字不要重复
        // 锁的粒度， 11号商品  product-11-lock , 12号 product-12-lock
        RLock lock = redisson.getLock("getCatalogJSon-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJSonFromDbWithRedisLock() {
        //TODO synchronized 本地锁只能锁本地进程，在分布式情况下想要锁住其他进程必须使用分布式锁
        //占分布式锁， 去redis
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {//加锁成功
            System.out.println("获取分布式锁成功。。。。。");
            //设置过期时间
            // redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //获取锁值+对比成功也要是原子操作， 否则也可能在数据传回的时候锁被删除 使用 Lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if(lockValue.equals(uuid)){
//                redisTemplate.delete("lock");//删除自己的锁
//            }
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                //删除锁
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            return dataFromDb;
        } else {//加锁失败
            //等待，休眠， 重试
            System.out.println("获取分布式锁失败。。。。。等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatalogJSonFromDbWithRedisLock();//
        }


    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {//缓存不为空直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("进同步块后查询了数据库。。。。。线程号===>" + Thread.currentThread().getId());

        /**
         * 1， 将数据库多次交互查询变为一次查询
         */
        //查询出所有的数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //2, 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(//封装一级分类的map集合
                (k) -> (k.getCatId().toString()),
                v -> {
                    //查询每个一级分类的二级分类
                    List<CategoryEntity> level2Categorys = getParent_cid(selectList, v.getCatId());
                    //封装每个一级分类的二级分类
                    List<Catelog2Vo> catelog2Vos = null;
                    if (level2Categorys != null) {
                        catelog2Vos = level2Categorys.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                            //封装三级分类
                            List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());
                            if (level3Categorys != null) {
                                List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Categorys.stream().map(l3 -> {
                                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName().toString());
                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(catelog3Vos);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }
        ));
        //将查询到到数据转为JSON,放一份到redis,
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    //从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatalogJSonFromDbWithLocalLock() {

        //加锁 ,只要是同一把锁，就能锁住所有线程
        //synchronized (this)
        //SpringBoot所有的组件在容器中都是单例的
        //TODO synchronized 本地锁只能锁本地进程，在分布式情况下想要锁住其他进程必须使用分布式锁
        synchronized (this) {
            //得到锁以后，应该再去缓存中确认一次，如果没有才需要继续查询， 有就不查询，
            return getDataFromDb();

        }

    }

    //查询集合中所有的parent_cid 为指定值的集合并返回
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }


}