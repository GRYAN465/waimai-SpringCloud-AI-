package com.cloudtakeout.order.client;

import com.cloudtakeout.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "cloud-service-product")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ApiResponse<Map<String, Object>> getProductById(@PathVariable("id") Long id);

    @PutMapping("/products/{id}/sales")
    ApiResponse<Map<String, Object>> increaseSalesAndDecreaseStock(@PathVariable("id") Long id,
                                                                   @RequestBody Map<String, Integer> request);
}
