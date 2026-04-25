package com.cloudtakeout.order.client;

import com.cloudtakeout.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "cloud-service-user")
public interface UserClient {
    @GetMapping("/users/{id}")
    ApiResponse<Map<String, Object>> getUserById(@PathVariable("id") Long id);
}
