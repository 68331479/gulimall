package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @Author: LDeng
 * @Date: 2020-12-25 14:26
 */

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;


    /*
    展示当前sku的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("准备查询"+skuId);
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item",vo);
        return "item";
    }
}
