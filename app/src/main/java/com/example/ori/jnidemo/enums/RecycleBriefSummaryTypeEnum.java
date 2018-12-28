package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2018/12/28 16:08
 * @description:
 */
public enum RecycleBriefSummaryTypeEnum {
    COMPLATE_SINGAL("0000", "光电3触发"),
    SCAN_VALIDATE_FAILD_USER_TAKE_BACK("0001", "扫码校验失败，用户取回物品"),
    SCAN_VALIDATE_SUCCESS_DONT_RECEIVE_COMPLATE_SINGAL("0002", "扫码成功，规定时间内光电 3 未触发"),
    FORCE_RECYCLE_SUCCESS("0003", "强制回收成功"),
    FORCE_RECYCLE_FAILD("0004", "强制回收失败"),
    ;

    private String code;

    private String name;

    RecycleBriefSummaryTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
