package com.cloudtakeout.product.service;

import com.cloudtakeout.product.entity.ProductEntity;
import com.cloudtakeout.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCacheService {

    private static final String PRODUCT_CACHE_KEY_PREFIX = "takeout:product:detail:";
    private static final String PRODUCT_NULL_MARKER = "__NULL__";
    private static final int PRODUCT_BASE_TTL_SECONDS = 600;
    private static final int PRODUCT_JITTER_SECONDS = 300;
    private static final int PRODUCT_NULL_TTL_SECONDS = 60;

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductCacheService(ProductRepository productRepository,
                               StringRedisTemplate redisTemplate,
                               ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public ProductEntity getProductById(Long id) {
        String cacheKey = cacheKey(id);
        String cachedValue = safeGet(cacheKey);
        if (cachedValue != null) {
            if (PRODUCT_NULL_MARKER.equals(cachedValue)) {
                return null;
            }
            try {
                return objectMapper.readValue(cachedValue, ProductEntity.class);
            } catch (Exception ignored) {
                // 缓存格式异常时回源数据库，并覆盖缓存
            }
        }

        ProductEntity product = productRepository.selectById(id);
        if (product == null) {
            safeSet(cacheKey, PRODUCT_NULL_MARKER, PRODUCT_NULL_TTL_SECONDS);
            return null;
        }

        try {
            String payload = objectMapper.writeValueAsString(product);
            int ttl = PRODUCT_BASE_TTL_SECONDS + ThreadLocalRandom.current().nextInt(PRODUCT_JITTER_SECONDS + 1);
            safeSet(cacheKey, payload, ttl);
        } catch (JsonProcessingException ignored) {
            // 序列化失败时不阻断主流程
        }
        return product;
    }

    public void evictProductCache(Long id) {
        try {
            redisTemplate.delete(cacheKey(id));
        } catch (Exception ignored) {
            // 缓存删失败不阻断订单主流程
        }
    }

    private String safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void safeSet(String key, String value, int ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Redis不可用时降级到数据库
        }
    }

    private String cacheKey(Long id) {
        return PRODUCT_CACHE_KEY_PREFIX + id;
    }
}
