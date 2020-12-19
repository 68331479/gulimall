package com.atguigu.gulimall.search.vo;

import lombok.Data;

/**
 * @Author: LDeng
 * @Date: 2020-12-19 13:24
 */
//封装检索条件
@Data
public class SearchParam {

    private String keyword;//页面传递过来的全文匹配关键字

}
