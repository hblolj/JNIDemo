package com.example.ori.jnidemo.bean;

import com.example.ori.jnidemo.enums.CategoryEnum;

/**
 * @author: hblolj
 * @date: 2018/12/25 19:47
 * @description:
 */
public class CategoryItem {

    private Integer itemId;

    private String icon;

    private String categoryName;

    private String unitPrice;

    private Double dPrice;

    private Boolean publicGood;

    private String unit;

    public CategoryItem() {
    }

    public CategoryItem(CategoryEnum categoryEnum) {
        this.itemId = categoryEnum.getId();
        this.categoryName = categoryEnum.getName();
        this.unitPrice = categoryEnum.getDescription();
        this.dPrice = categoryEnum.getUnitPrice();
        this.unit = categoryEnum.getUnit();
    }

    public CategoryItem(String categoryName, String unitPrice) {
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
    }

    public CategoryItem(Integer itemId, String categoryName, String unitPrice) {
        this.itemId = itemId;
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
    }

    public CategoryItem(String icon, String categoryName, String unitPrice, Boolean publicGood) {
        this.icon = icon;
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
        this.publicGood = publicGood;
    }

    public CategoryItem(Integer itemId, String icon, String categoryName, String unitPrice, Boolean publicGood) {
        this.itemId = itemId;
        this.icon = icon;
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
        this.publicGood = publicGood;
    }

    @Override
    public String toString() {
        return "CategoryItem{" +
                "itemId=" + itemId +
                ", icon='" + icon + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", unitPrice='" + unitPrice + '\'' +
                ", dPrice=" + dPrice +
                ", publicGood=" + publicGood +
                ", unit='" + unit + '\'' +
                '}';
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getPublicGood() {
        return publicGood;
    }

    public void setPublicGood(Boolean publicGood) {
        this.publicGood = publicGood;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Double getdPrice() {
        return dPrice;
    }

    public void setdPrice(Double dPrice) {
        this.dPrice = dPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
