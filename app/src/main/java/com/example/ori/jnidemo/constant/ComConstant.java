package com.example.ori.jnidemo.constant;

import com.example.ori.jnidemo.enums.CategoryEnum;

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

    // 开启用户回收门操作码
    public static final String OPEN_USER_RECYCLE_ACTION_CODE = "B1";
    // 开启用户回收门结果反馈操作码
    public static final String OPEN_USER_RECYCLE_RESULT_ACTION_CODE = "B2";

    // 关闭用户回收门操作码
    public static final String CLOSE_USER_RECYCLE_ACTION_CODE = "B3";
    // 关闭用户回收门结果反馈操作码
    public static final String CLOSE_USER_RECYCLE_RESULT_ACTION_CODE = "B4";

    // 开启管理员回收门操作码
    public static final String OPEN_ADMIN_RECYCLE_ACTION_CODE = "B5";
    // 开启管理员回收门结果反馈操作码
    public static final String OPEN_ADMIN_RECYCLE_RESULT_ACTION_CODE = "B6";

    // 关闭管理员回收门操作码
    public static final String CLOSE_ADMIN_RECYCLE_ACTION_CODE = "B7";
    // 关闭管理员回收门结果反馈操作码
    public static final String CLOSE_ADMIN_RECYCLE_RESULT_ACTION_CODE = "B8";

    // 刷新延时关门任务操作码
    public static final String REFRESH_DOOR_CLOESE_ACTION_CODE = "B9";
    // 刷新延时关门任务反馈操作码
    public static final String REFRESH_DOOR_CLOESE_RESULT_ACTION_CODE = "BA";

    // 条码扫描结果校验操作码
    public static final String BAR_CODE_SCAN_VALIDATE_ACTION_CODE = "BB";
    // 条码扫描结果校验结果反馈操作码
    public static final String BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE = "BC";

    // 通信异常通知操作码
    public static final String COMMUNICATION_EXCEPTION_NOTICE_ACTION_CODE = "BD";
    // 通信异常通知反馈操作码
    public static final String COMMUNICATION_EXCEPTION_NOTICE_RESULT_ACTION_CODE = "BE";

    // 物品投递结果通知操作码
    public static final String RECYCLE_RESULT_NOTICE_ACTION_CODE = "BF";
    // 物品投递结果通知反馈操作码
    public static final String RECYCLE_RESULT_NOTICE_RESULT_ACTION_CODE = "C0";

    // 投递物强制回收操作码
    public static final String FORCE_RECYCLE_ACTION_CODE = "C1";
    // 投递物强制回收操作反馈码
    public static final String FORCE_RECYCLE_RESULT_ACTION_CODE = "C2";

    // 称重操作码
    public static final String WEIGH_ACTION_CODE = "C3";
    // 称重结果反馈码
    public static final String WEIGH_RESULT_ACTION_CODE = "C4";

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

    public static String getAddressCodeByCategoryItemId(Integer itemId){
        if (CategoryEnum.METAL_REGENERANT.getId().equals(itemId)){
            // 金属
            return METAL_RECYCLE_IC_ADDRESS;
        }else if (CategoryEnum.PAPER_REGENERANT.getId().equals(itemId)){
            // 纸类
            return PAPER_RECYCLE_IC_ADDRESS;
        }else if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(itemId)){
            // 塑料瓶
            return PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS;
        }
        return null;
    }
}
