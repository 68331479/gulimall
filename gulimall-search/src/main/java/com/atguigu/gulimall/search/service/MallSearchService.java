package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam; /**
 * @Author: LDeng
 * @Date: 2020-12-19 13:28
 */
public interface MallSearchService {

    /**
     * 检索的所有参数
     * @param param
     * @return 返回检索结果
     */
    Object search(SearchParam param);
}
