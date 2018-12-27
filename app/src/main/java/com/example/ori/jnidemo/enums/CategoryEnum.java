package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2018/12/27 15:55
 * @description:
 */
public enum CategoryEnum {

    METAL_REGENERANT(1, "金属", "0.60元/公斤"),
    PLASTIC_REGENERANT(2, "塑料", "0.70元/公斤"),
    TEXTILE_REGENERANT(3, "纺织物", "0.20元/公斤"),
    PLASTIC_BOTTLE__REGENERANT(4, "饮料瓶", "0.04元/个"),
    PAPER_REGENERANT(5, "纸类", "0.70元/公斤"),
    GLASS_REGENERANT(6, "玻璃", ""),
    HARMFUL_WASTE(7, "有害垃圾", ""),
    ;

    private Integer id;

    private String name;

    private String price;

    CategoryEnum() {
    }

    CategoryEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    CategoryEnum(Integer id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }
}
