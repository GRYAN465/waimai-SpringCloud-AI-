package com.cloudtakeout.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudtakeout.common.api.ApiResponse;
import com.cloudtakeout.order.client.ProductClient;
import com.cloudtakeout.order.client.UserClient;
import com.cloudtakeout.order.dto.PlaceOrderRequest;
import com.cloudtakeout.order.entity.OrderEntity;
import com.cloudtakeout.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public OrderController(OrderRepository orderRepository,
                           UserClient userClient,
                           ProductClient productClient,
                           RabbitTemplate rabbitTemplate,
                           StringRedisTemplate redisTemplate) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    @SentinelResource(
            value = "placeOrderResource",
            blockHandler = "placeOrderBlocked",
            fallback = "placeOrderFallback"
    )
    public ApiResponse<OrderEntity> placeOrder(@RequestBody PlaceOrderRequest request) {
        ApiResponse<Map<String, Object>> userResponse = userClient.getUserById(request.getUserId());
        if (userResponse.getCode() != 0) {
            return ApiResponse.fail("下单失败：用户不存在");
        }

        ApiResponse<Map<String, Object>> productResponse = queryProduct(request.getProductId());
        if (productResponse.getCode() != 0) {
            return ApiResponse.fail("下单失败：商品不存在");
        }

        Map<String, Object> product = productResponse.getData();
        BigDecimal price = new BigDecimal(product.get("price").toString());
        BigDecimal total = price.multiply(BigDecimal.valueOf(request.getCount()));

        Map<String, Integer> updateSalesRequest = new HashMap<>();
        updateSalesRequest.put("count", request.getCount());
        ApiResponse<Map<String, Object>> updateSalesResponse =
                productClient.increaseSalesAndDecreaseStock(request.getProductId(), updateSalesRequest);
        if (updateSalesResponse.getCode() != 0) {
            return ApiResponse.fail("下单失败：" + updateSalesResponse.getMessage());
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(request.getUserId());
        orderEntity.setProductId(request.getProductId());
        orderEntity.setCount(request.getCount());
        orderEntity.setTotalAmount(total);
        orderEntity.setStatus("CREATED");
        orderEntity.setOrderTime(LocalDateTime.now());
        orderEntity.setRemark(request.getRemark());
        orderRepository.insert(orderEntity);
        OrderEntity saved = orderEntity;

        // 下单会影响销量/库存等展示信息，主动失效商品详情缓存
        try {
            redisTemplate.delete("takeout:product:detail:" + request.getProductId());
        } catch (Exception ignored) {
            // 缓存删失败不阻断下单主流程
        }

        rabbitTemplate.convertAndSend(
                "takeout.order.exchange",
                "takeout.order.created",
                "{\"orderId\":" + saved.getId() + ",\"status\":\"CREATED\"}"
        );
        return ApiResponse.success(saved);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderEntity>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderEntity> orders = orderRepository.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, userId)
                        .orderByDesc(OrderEntity::getOrderTime)
        );
        return ApiResponse.success(orders);
    }

    @SentinelResource(value = "queryProductResource", fallback = "queryProductFallback")
    public ApiResponse<Map<String, Object>> queryProduct(Long productId) {
        return productClient.getProductById(productId);
    }

    public ApiResponse<OrderEntity> placeOrderBlocked(PlaceOrderRequest request, BlockException ex) {
        return ApiResponse.fail("请求过于频繁，请稍后再试");
    }

    public ApiResponse<OrderEntity> placeOrderFallback(PlaceOrderRequest request, Throwable ex) {
        return ApiResponse.fail("订单服务降级，暂时无法下单");
    }

    public ApiResponse<Map<String, Object>> queryProductFallback(Long productId, Throwable ex) {
        return ApiResponse.fail("商品服务暂时不可用，已触发熔断");
    }
}
