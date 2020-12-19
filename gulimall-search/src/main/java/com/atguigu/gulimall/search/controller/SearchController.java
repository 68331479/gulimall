package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: LDeng
 * @Date: 2020-12-18 16:01
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String ListPage(SearchParam param){

        Object result = mallSearchService.search(param);
        return "list";
    }
}
