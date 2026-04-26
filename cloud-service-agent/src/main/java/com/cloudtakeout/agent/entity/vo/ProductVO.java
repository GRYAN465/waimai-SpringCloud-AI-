package com.cloudtakeout.agent.entity.vo;

import java.math.BigDecimal;

public class ProductVO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String spicyLevel;
    private Integer sales;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getSpicyLevel() {
        return spicyLevel;
    }

    public void setSpicyLevel(String spicyLevel) {
        this.spicyLevel = spicyLevel;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
