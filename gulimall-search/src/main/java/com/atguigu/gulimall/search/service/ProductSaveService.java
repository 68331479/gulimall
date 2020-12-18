package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List; /**
 * @Author: LDeng
 * @Date: 2020-12-11 15:00
 */
public interface ProductSaveService {


    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
