package com.cloudtakeout.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudtakeout.auth.dto.LoginRequest;
import com.cloudtakeout.auth.dto.RegisterRequest;
import com.cloudtakeout.auth.entity.AuthUserEntity;
import com.cloudtakeout.auth.repository.AuthUserRepository;
import com.cloudtakeout.auth.util.JwtTokenUtil;
import com.cloudtakeout.common.api.ApiResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthUserRepository authUserRepository;

    public AuthController(JwtTokenUtil jwtTokenUtil, AuthUserRepository authUserRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authUserRepository = authUserRepository;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody LoginRequest request) {
        AuthUserEntity user = authUserRepository.selectOne(
                new LambdaQueryWrapper<AuthUserEntity>()
                        .eq(AuthUserEntity::getUsername, request.getUsername())
        );
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return ApiResponse.fail("用户名或密码错误");
        }
        String token = jwtTokenUtil.generateToken(user.getUsername());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("userId", String.valueOf(user.getId()));
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        return ApiResponse.success(result);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return ApiResponse.fail("用户名和密码不能为空");
        }
        AuthUserEntity exists = authUserRepository.selectOne(
                new LambdaQueryWrapper<AuthUserEntity>()
                        .eq(AuthUserEntity::getUsername, request.getUsername())
        );
        if (exists != null) {
            return ApiResponse.fail("用户名已存在");
        }

        AuthUserEntity user = new AuthUserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setName(StringUtils.hasText(request.getName()) ? request.getName() : request.getUsername());
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone() : "");
        user.setAvatar("https://picsum.photos/120/120?random=" + request.getUsername().hashCode());
        user.setBio("这个人很神秘，什么都没写");
        authUserRepository.insert(user);

        String token = jwtTokenUtil.generateToken(user.getUsername());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("userId", String.valueOf(user.getId()));
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        return ApiResponse.success(result);
    }
}
