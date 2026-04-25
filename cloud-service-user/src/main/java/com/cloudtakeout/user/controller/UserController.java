package com.cloudtakeout.user.controller;

import com.cloudtakeout.common.api.ApiResponse;
import com.cloudtakeout.user.entity.UserEntity;
import com.cloudtakeout.user.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserEntity> getUserById(@PathVariable Long id) {
        UserEntity user = userRepository.selectById(id);
        return user == null ? ApiResponse.fail("用户不存在") : ApiResponse.success(user);
    }
}
