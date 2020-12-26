package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author ldeng
 * @email 68331479@qq.com
 * @date 2020-11-12 10:27:38
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
