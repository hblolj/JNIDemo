package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2019/1/3 15:46
 * @description:
 */
public enum WeighTypeEnum {
    PREFIX_WEIGH("0000", "开门前置称重"),
    SUFFIX_WEIGH("0001", "关门后置称重"),
    ;

    private String code;

    private String name;

    WeighTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
