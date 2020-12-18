package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: LDeng
 * @Date: 2020-12-18 16:01
 */
@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String ListPage(){

        return "list";
    }
}
