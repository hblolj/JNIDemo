package com.example.ori.jnidemo.enums;

import java.math.BigDecimal;

/**
 * @author: hblolj
 * @date: 2018/12/27 15:55
 * @description:
 */
public enum CategoryEnum {

    METAL_REGENERANT(1, "金属", new BigDecimal("0.6"), "0.60元/公斤", "公斤"),
    PLASTIC_REGENERANT(2, "塑料", new BigDecimal("0.7"), "0.70元/公斤", "公斤"),
    TEXTILE_REGENERANT(3, "纺织物", new BigDecimal("0.2"), "0.20元/公斤", "公斤"),
    PLASTIC_BOTTLE_REGENERANT(4, "饮料瓶", new BigDecimal("0.04"), "0.04元/个", "个"),
    PAPER_REGENERANT(5, "纸类", new BigDecimal("0.7"), "0.70元/公斤", "公斤"),
    GLASS_REGENERANT(6, "玻璃", new BigDecimal("0"), ""),
    HARMFUL_WASTE(7, "有害垃圾", new BigDecimal("0"), ""),
    ;

    private Integer id;

    private String name;

    private BigDecimal unitPrice;

    private String description;

    private String unit;

    CategoryEnum() {
    }

    CategoryEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    CategoryEnum(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    CategoryEnum(Integer id, String name, BigDecimal unitPrice, String description) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.description = description;
    }

    CategoryEnum(Integer id, String name, BigDecimal unitPrice, String description, String unit) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.description = description;
        this.unit = unit;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }
}
