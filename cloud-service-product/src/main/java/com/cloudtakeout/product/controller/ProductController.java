package com.cloudtakeout.product.controller;

import com.cloudtakeout.common.api.ApiResponse;
import com.cloudtakeout.product.dto.UpdateSalesRequest;
import com.cloudtakeout.product.entity.ProductEntity;
import com.cloudtakeout.product.repository.ProductRepository;
import com.cloudtakeout.product.service.ProductCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    public ProductController(ProductRepository productRepository, ProductCacheService productCacheService) {
        this.productRepository = productRepository;
        this.productCacheService = productCacheService;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductEntity> getProductById(@PathVariable Long id) {
        ProductEntity product = productCacheService.getProductById(id);
        if (product == null) {
            return ApiResponse.fail("商品不存在");
        }
        return ApiResponse.success(product);
    }

    @GetMapping
    public ApiResponse<List<ProductEntity>> listProducts() {
        return ApiResponse.success(productRepository.selectList(null));
    }

    @PutMapping("/{id}/sales")
    public ApiResponse<ProductEntity> increaseSalesAndDecreaseStock(@PathVariable Long id,
                                                                     @RequestBody UpdateSalesRequest request) {
        ProductEntity product = productRepository.selectById(id);
        if (product == null) {
            return ApiResponse.fail("商品不存在");
        }
        int count = request.getCount() == null ? 0 : request.getCount();
        if (count <= 0) {
            return ApiResponse.fail("数量必须大于0");
        }
        if (product.getStock() < count) {
            return ApiResponse.fail("库存不足");
        }

        product.setStock(product.getStock() - count);
        product.setSales((product.getSales() == null ? 0 : product.getSales()) + count);
        productRepository.updateById(product);
        productCacheService.evictProductCache(id);
        return ApiResponse.success(product);
    }
}
