package com.cloudtakeout.product.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtakeout.product.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductRepository extends BaseMapper<ProductEntity> {
}
