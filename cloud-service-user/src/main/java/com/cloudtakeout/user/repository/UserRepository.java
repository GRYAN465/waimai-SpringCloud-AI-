package com.cloudtakeout.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtakeout.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<UserEntity> {
}
