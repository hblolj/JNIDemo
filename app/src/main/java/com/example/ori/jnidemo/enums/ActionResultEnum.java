package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2018/12/28 10:45
 * @description:
 */
public enum ActionResultEnum {

    OPEN_DOOR_SUCCESS(1, "开门成功"),
    OPEN_DOOR_FAILD(2, "开门失败"),
    CLOSE_DOOR_SUCCESS(3, "正常关门成功"),
    PREFIX_CLOSE_DOOR_SUCCESS(6, "强制回收前置关门成功"),
    CLOSE_DOOR_FAILD(4, "关门失败"),
    REFRESH_CLOSE_DOOR_COUNT_DOWN_TIME(5, "刷新关门倒计时"),
    PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_SUCCESS(7, "塑料瓶扫码校验成功"),
    PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD(8, "塑料瓶扫码校验失败"),
    RECYCLE_BRIEF_SUMMARY(9, "结算"),
    RECYCLE_COMPLATE(10, "投递完成"),
    USER_TAKE_BACK(11, "用户取回投递物"),
    RECYCLE_COMPLATE_VALIDATE_FAILD(12, "光电 3 未触发"),
    FORCE_RECYCLE_SUCCESS(13, "强制回收成功"),
    FORCE_RECYCLE_FAILD(14, "强制回收失败"),
    ;

    private Integer resultCode;

    private String resultName;

    ActionResultEnum() {
    }

    ActionResultEnum(Integer resultCode, String resultName) {
        this.resultCode = resultCode;
        this.resultName = resultName;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }
}
