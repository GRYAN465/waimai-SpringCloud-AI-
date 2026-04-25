package com.cloudtakeout.product.controller;

import com.cloudtakeout.common.api.ApiResponse;
import com.cloudtakeout.product.entity.ProductEntity;
import com.cloudtakeout.product.repository.ProductRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    public ProductController(ProductRepository productRepository, StringRedisTemplate redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductEntity> getProductById(@PathVariable Long id) {
        String cacheKey = "takeout:product:" + id;
        String cachedName = redisTemplate.opsForValue().get(cacheKey);

        ProductEntity product = productRepository.selectById(id);
        if (product == null) {
            return ApiResponse.fail("商品不存在");
        }

        if (cachedName == null) {
            redisTemplate.opsForValue().set(cacheKey, product.getName(), 10, TimeUnit.MINUTES);
        }
        return ApiResponse.success(product);
    }
}
