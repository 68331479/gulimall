package com.atguigu.gulimall.product;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    CategoryService categoryService;


    @Test
    public void testUpload() throws Exception{
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-shanghai.aliyuncs.com";
// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4G7njiBrj5NQn8Gd92B2";
        String accessKeySecret = "5qIuGN3ygTyQyKWoBX56ezSL2GW6Lu";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\SGG\\xiaomi.png");
        ossClient.putObject("intersurgical", "xiaomi.png", inputStream);

// 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成");
    }

    @Test
    void contextLoads() {
          BrandEntity brandEntity = new BrandEntity();
          brandEntity.setBrandId(1L);
          brandEntity.setDescript("xxxxx");
//        brandEntity.setDescript("abc");
//        brandEntity.setName("华为");
//
//        brandService.save(brandEntity);
//        System.out.println("保存成功。。。。");
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.stream().forEach(System.out::println);

    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
        System.out.println(Arrays.asList(catelogPath));
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Test
    public void testRedis(){
        //k:hello  v:world
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //保存
        ops.set("hello","world"+ UUID.randomUUID().toString());
        //查询
        String s = ops.get("hello");
        System.out.println("保存的数据是"+s);
    }

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }

}
