package com.cloudtakeout.agent.entity.query;

import org.springframework.ai.tool.annotation.ToolParam;

public class MenuQuery {
    @ToolParam(required = false, description = "辣度偏好，例如不辣、微辣、中辣、重辣")
    private String spicyLevel;

    @ToolParam(required = false, description = "菜品关键词，例如鸡、牛、饭")
    private String keyword;

    public String getSpicyLevel() {
        return spicyLevel;
    }

    public void setSpicyLevel(String spicyLevel) {
        this.spicyLevel = spicyLevel;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
