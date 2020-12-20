package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @Author: LDeng
 * @Date: 2020-12-19 13:28
 */
public interface MallSearchService {

    /**
     * 检索的所有参数
     * @param param
     * @return 返回检索结果 , 里面包含页面所有需要的所有信息
     *
     */
    SearchResult search(SearchParam param);
}
