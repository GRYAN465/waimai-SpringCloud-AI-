package com.cloudtakeout.auth.controller;

import com.cloudtakeout.auth.dto.LoginRequest;
import com.cloudtakeout.auth.util.JwtTokenUtil;
import com.cloudtakeout.common.api.ApiResponse;
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

    public AuthController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody LoginRequest request) {
        if (!"demo".equals(request.getUsername()) || !"123456".equals(request.getPassword())) {
            return ApiResponse.fail("用户名或密码错误");
        }
        String token = jwtTokenUtil.generateToken(request.getUsername());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        return ApiResponse.success(result);
    }
}
