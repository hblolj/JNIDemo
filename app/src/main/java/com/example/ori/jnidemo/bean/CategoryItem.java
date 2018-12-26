package com.example.ori.jnidemo.bean;

/**
 * @author: hblolj
 * @date: 2018/12/25 19:47
 * @description:
 */
public class CategoryItem {

    private String icon;

    private String categoryName;

    private String unitPrice;

    private Boolean publicGood;

    public CategoryItem() {
    }

    public CategoryItem(String categoryName, String unitPrice) {
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
    }

    public CategoryItem(String icon, String categoryName, String unitPrice, Boolean publicGood) {
        this.icon = icon;
        this.categoryName = categoryName;
        this.unitPrice = unitPrice;
        this.publicGood = publicGood;
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
}
