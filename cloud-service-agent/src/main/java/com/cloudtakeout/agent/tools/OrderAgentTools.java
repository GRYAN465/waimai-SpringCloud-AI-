package com.cloudtakeout.agent.tools;

import com.cloudtakeout.agent.entity.query.MenuQuery;
import com.cloudtakeout.agent.entity.query.OrderItemRequest;
import com.cloudtakeout.agent.entity.vo.ProductVO;
import com.cloudtakeout.common.api.ApiResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OrderAgentTools {

    private final RestClient restClient;
    private final String userBaseUrl;
    private final String productBaseUrl;
    private final String orderBaseUrl;

    public OrderAgentTools(RestClient restClient,
                           @Value("${agent.service.user-base-url}") String userBaseUrl,
                           @Value("${agent.service.product-base-url}") String productBaseUrl,
                           @Value("${agent.service.order-base-url}") String orderBaseUrl) {
        this.restClient = restClient;
        this.userBaseUrl = userBaseUrl;
        this.productBaseUrl = productBaseUrl;
        this.orderBaseUrl = orderBaseUrl;
    }

    @Tool(description = "查询用户口味偏好")
    public String queryUserTastePreference(@ToolParam(description = "用户ID") Long userId) {
        ApiResponse<Map<String, Object>> resp = restClient.get()
                .uri(userBaseUrl + "/users/{id}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                });
        if (resp == null || resp.getCode() != 0 || resp.getData() == null) {
            return "未查询到用户口味偏好";
        }
        Object taste = resp.getData().get("tastePreference");
        return taste == null || String.valueOf(taste).isBlank() ? "用户尚未设置口味偏好" : String.valueOf(taste);
    }

    @Tool(description = "查询菜单并按条件筛选，默认返回销量最高的菜品")
    public List<ProductVO> queryMenu(@ToolParam(description = "菜单筛选条件", required = false) MenuQuery query) {
        ApiResponse<List<ProductVO>> resp = restClient.get()
                .uri(productBaseUrl + "/products")
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<List<ProductVO>>>() {
                });
        if (resp == null || resp.getCode() != 0 || resp.getData() == null) {
            return List.of();
        }

        List<ProductVO> products = resp.getData().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (query != null && query.getSpicyLevel() != null && !query.getSpicyLevel().isBlank()) {
            String spicy = query.getSpicyLevel().trim();
            products = products.stream()
                    .filter(p -> p.getSpicyLevel() != null && p.getSpicyLevel().contains(spicy))
                    .collect(Collectors.toList());
        }
        if (query != null && query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim().toLowerCase(Locale.ROOT);
            products = products.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(keyword))
                    .collect(Collectors.toList());
        }

        return products.stream()
                .sorted(Comparator.comparing((ProductVO p) -> p.getSales() == null ? 0 : p.getSales()).reversed())
                .limit(10)
                .toList();
    }

    @Tool(description = "创建多菜品订单，会依次调用订单服务创建订单")
    public List<String> createBatchOrders(@ToolParam(description = "用户ID") Long userId,
                                          @ToolParam(description = "下单项列表，包含菜品名称和数量") List<OrderItemRequest> items,
                                          @ToolParam(description = "备注", required = false) String remark) {
        if (items == null || items.isEmpty()) {
            return List.of("下单失败：未提供菜品列表");
        }

        Map<String, ProductVO> productByName = queryMenu(null).stream()
                .collect(Collectors.toMap(ProductVO::getName, p -> p, (a, b) -> a));
        List<String> result = new ArrayList<>();
        for (OrderItemRequest item : items) {
            if (item == null || item.getDishName() == null || item.getDishName().isBlank()) {
                result.add("下单失败：存在空菜品名称");
                continue;
            }
            ProductVO product = productByName.get(item.getDishName());
            if (product == null) {
                result.add("下单失败：未找到菜品[" + item.getDishName() + "]");
                continue;
            }
            Integer count = item.getCount() == null ? 1 : item.getCount();
            ApiResponse<Map<String, Object>> orderResp = restClient.post()
                    .uri(orderBaseUrl + "/orders")
                    .body(Map.of(
                            "userId", userId,
                            "productId", product.getId(),
                            "count", count,
                            "remark", remark == null ? "AI助手下单" : remark
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                    });
            if (orderResp != null && orderResp.getCode() == 0 && orderResp.getData() != null) {
                result.add("下单成功：" + item.getDishName() + " x" + count + "，订单ID=" + orderResp.getData().get("id"));
            } else {
                String msg = orderResp == null ? "订单服务无响应" : orderResp.getMessage();
                result.add("下单失败：" + item.getDishName() + "，原因：" + msg);
            }
        }
        return result;
    }
}
