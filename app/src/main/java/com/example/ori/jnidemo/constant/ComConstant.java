package com.example.ori.jnidemo.constant;

public class ComConstant {

    /**
     * 指令发送最大重试次数
     */
    public static final Integer MAX_RETRY_COUNT = 3;

    //------------------------------通信地址--------------------------------------

    /**
     * Android 机地址
     */
    public static final String ANDROID_ADDRESS = "A1";
    /**
     * 塑料瓶回收 IC 地址
     */
    public static final String PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS = "A2";
    /**
     * 金属回收 IC 地址
     */
    public static final String METAL_RECYCLE_IC_ADDRESS = "A3";
    /**
     * 纸类回收 IC 地址
     */
    public static final String PAPER_RECYCLE_IC_ADDRESS = "A4";

    //-------------------------------操作码---------------------------------------

    /**
     * 开启用户回收门操作码
     */
    public static final String OPEN_USER_RECYCLE_ACTION_CODE = "B1";
    /**
     * 关闭用户回收门操作码
     */
    public static final String CLOSE_USER_RECYCLE_ACTION_CODE = "B2";
    /**
     * 开启管理员回收门操作码
     */
    public static final String OPEN_ADMIN_RECYCLE_ACTION_CODE = "B3";
    /**
     * 关闭管理员回收门操作码
     */
    public static final String CLOSE_ADMIN_RECYCLE_ACTION_CODE = "B4";
    /**
     * 称重指令
     */
    public static final String WEIGH_ACTION_CODE = "D1";

    public static String getAddressCodeByName(String addressName){
        switch (addressName){
            case "塑料回收机": return PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS;
            case "金属回收机": return METAL_RECYCLE_IC_ADDRESS;
            case "纸类回收机":return PAPER_RECYCLE_IC_ADDRESS;
            default: return null;
        }
    }

    public static String getActionCodeByName(String actionName){
        switch (actionName){
            case "开用户回收门": return OPEN_USER_RECYCLE_ACTION_CODE;
            case "关用户回收门": return CLOSE_USER_RECYCLE_ACTION_CODE;
            case "开管理员回收门":return OPEN_ADMIN_RECYCLE_ACTION_CODE;
            case "关管理员回收门":return CLOSE_ADMIN_RECYCLE_ACTION_CODE;
            case "称重":return WEIGH_ACTION_CODE;
            default: return null;
        }
    }
}
