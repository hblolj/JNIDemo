package com.example.ori.jnidemo.enums;

import java.math.BigDecimal;

/**
 * @author: hblolj
 * @date: 2018/12/27 15:55
 * @description:
 */
public enum CategoryEnum {

    METAL_REGENERANT(1, "金属", "Metal", new BigDecimal("0.6"), "/Kg", "元/公斤"),
    PLASTIC_REGENERANT(2, "塑料", "Plastic", new BigDecimal("0.7"), "/Kg", "元/公斤"),
    TEXTILE_REGENERANT(3, "纺织物", "Textiles", new BigDecimal("0.2"), "/Kg", "元/公斤"),
    PLASTIC_BOTTLE_REGENERANT(4, "饮料瓶", "Bottle", new BigDecimal("0.04"), "/PC", "元/个"),
    PAPER_REGENERANT(5, "纸类", "Paper", new BigDecimal("0.7"), "/Kg", "元/公斤"),
    GLASS_REGENERANT(6, "玻璃", "Glass", new BigDecimal("0"), "/Kg", "元/公斤"),
    HARMFUL_WASTE(7, "有害垃圾", "Harmful Garbage", new BigDecimal("0"), "/Kg", "元/公斤"),
    ;

    private Integer id;

    private String name;

    private String enName;

    private BigDecimal unitPrice;

    private String enUnit;

    private String unit;

    CategoryEnum() {
    }

    CategoryEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    CategoryEnum(Integer id, String name, String enName, BigDecimal unitPrice, String enUnit, String unit) {
        this.id = id;
        this.name = name;
        this.enName = enName;
        this.unitPrice = unitPrice;
        this.enUnit = enUnit;
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

    public String getUnit() {
        return unit;
    }

    public String getEnName() {
        return enName;
    }

    public String getEnUnit() {
        return enUnit;
    }
}
