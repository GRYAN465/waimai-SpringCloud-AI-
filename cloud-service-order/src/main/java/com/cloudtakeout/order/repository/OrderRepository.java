package com.cloudtakeout.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtakeout.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderRepository extends BaseMapper<OrderEntity> {
}
