package com.cloudtakeout.agent.entity.query;

import org.springframework.ai.tool.annotation.ToolParam;

public class OrderItemRequest {
    @ToolParam(description = "菜品名称")
    private String dishName;
    @ToolParam(description = "数量")
    private Integer count;

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
