package com.cloudtakeout.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtakeout.auth.entity.AuthUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthUserRepository extends BaseMapper<AuthUserEntity> {
}
