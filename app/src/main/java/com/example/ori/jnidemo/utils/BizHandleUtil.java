package com.example.ori.jnidemo.utils;

import android.os.Handler;
import android.util.Log;

import com.example.ori.jnidemo.HomeActivity;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.enums.ActionResultEnum;
import com.example.ori.jnidemo.enums.CloseDoorType;
import com.example.ori.jnidemo.enums.RecycleBriefSummaryTypeEnum;
import com.example.ori.jnidemo.enums.WeighTypeEnum;
import com.example.ori.jnidemo.utils.barcode.BarCodeScanUtil;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;

import static com.example.ori.jnidemo.enums.RecycleBriefSummaryTypeEnum.SCAN_VALIDATE_SUCCESS_DONT_RECEIVE_COMPLATE_SINGAL;

/**
 * @author: hblolj
 * @date: 2019/1/2 10:00
 * @description: 业务数据处理
 */
public class BizHandleUtil {

    private static final String TAG = BizHandleUtil.class.getSimpleName();
    /**
     * 塑料瓶回收完成确认延迟任务(光电 3 信号延迟检验)
     */
    private static Runnable PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK = null;
    /**
     * 塑料瓶回收完成确认延迟任务(光电 3 信号延迟检验) 延迟时间
     */
    private static final Long PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TIME = 30000L;

    /**
     * 开门结果处理，包括了塑料瓶、金属、纸类回收的开门结果
     * @param event
     */
    public static void handleOpenDoorResult(MessageEvent event) {
        SerialHelper.waitResults.remove(event.getKey());
        Boolean openDoorResult = (Boolean) event.getMessage();
        if (openDoorResult){
            // 开门成功
            BarCodeScanUtil.getInstance().clearData();
        }
        // 通知 RecyclerFragment 开门结果
        EventBus.getDefault().post(openDoorResult ? ActionResultEnum.OPEN_DOOR_SUCCESS : ActionResultEnum.OPEN_DOOR_FAILD);
    }

    /**
     * 关门结果处理
     * @param event
     * @param comHelper
     * @param myHandler
     */
    public static void handleCloseDoorResult(MessageEvent event, SerialHelper comHelper, Handler myHandler) {
        SerialHelper.waitResults.remove(event.getKey());
        Boolean closeDoorResult = (Boolean) event.getMessage();
        if (closeDoorResult){
            // 关门成功
            BarCodeScanUtil.getInstance().clearData();
            Integer closeDoorType = (Integer) event.getExtra();

            if (CloseDoorType.FORCE_RECYCLE_PREFIX.getTypeId().equals(closeDoorType)){
                // 强制回收前置关门结果
                EventBus.getDefault().post(ActionResultEnum.PREFIX_CLOSE_DOOR_SUCCESS);
                // 发送强制回收指令
                OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                        ComConstant.FORCE_RECYCLE_ACTION_CODE, null, myHandler, 1);
                return;
            }else if (CloseDoorType.WEIGH_PREFIX.getTypeId().equals(closeDoorType)){
                // 称重前置关门成功
                EventBus.getDefault().post(ActionResultEnum.WEIGH_PREFIX_CLOSE_DOOR_SUCCESS);
                return;
            }
        }
        EventBus.getDefault().post(closeDoorResult ? ActionResultEnum.CLOSE_DOOR_SUCCESS : ActionResultEnum.CLOSE_DOOR_FAILD);
    }

    /**
     * 扫码请求处理
     * @param event
     * @param comHelper
     * @param myHandler
     */
    public static void handleScanRequest(MessageEvent event, SerialHelper comHelper, Handler myHandler) {
        Boolean scanResult = BarCodeScanUtil.getInstance().validateScanResult();
        if (scanResult){
            // 开启一个光电 3 检测延时，延时触发，走单次回收结束流程
            startRecycleComplteConfirmDelayTask(myHandler);
        }
        // 返回扫码结果给 IC
        OrderUtils.sendOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE, scanResult ? "FFFF" : "0000", myHandler, 1);
        // 不管扫码成功还是失败，界面上都是设置投递完成按钮为不可点击状态
        EventBus.getDefault().post(scanResult ? ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_SUCCESS : ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD);
    }

    /**
     * 处理单次回收完毕信息
     * @param event
     * @param myHandler
     */
    public static void handleSingleRecoveryComplateMessage(MessageEvent event, Handler myHandler) {
        String param = (String) event.getMessage();
        if (RecycleBriefSummaryTypeEnum.COMPLATE_SINGAL.getCode().equals(param)){
            // 光电 3 触发
            // 1. 移除光电 3 校验延时任务
            clearTask(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK, myHandler);
            // 2. 检查扫码结果，进行消消乐
            HomeActivity.TOTAL_PLASTIC_BOTTLE_NUM++;
            if (BarCodeScanUtil.getInstance().validateScanResult()){
                // 移除第一个扫码结果，有效投递数 + 1
                BarCodeScanUtil.getInstance().consumeScanResult();
                // 当次投递数 + 1
                HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM++;
                HomeActivity.TOTAL_VALID_PLASTIC_BOTTLE_NUM++;
            }
            // 4. 通知 RecycleFragment 更新，界面显示切换 -> 当前投递数
            EventBus.getDefault().post(ActionResultEnum.RECYCLE_BRIEF_SUMMARY);
        }else if (RecycleBriefSummaryTypeEnum.SCAN_VALIDATE_FAILD_USER_TAKE_BACK.getCode().equals(param)){
            // 扫码失败，用户成功取回
            EventBus.getDefault().post(ActionResultEnum.USER_TAKE_BACK);
        }else if (SCAN_VALIDATE_SUCCESS_DONT_RECEIVE_COMPLATE_SINGAL.getCode().equals(param)){
            // 扫码成功，光电 3 未触发
            EventBus.getDefault().post(ActionResultEnum.RECYCLE_COMPLATE_VALIDATE_FAILD);
        }
    }

    /**
     * 处理称重结果
     * @param event
     */
    public static void handleWeighResultMessage(MessageEvent event) {
        Boolean result = (Boolean) event.getMessage();
        if (result){
            // 称重成功
            String extra = (String) event.getExtra();
            String source = extra.substring(0, 2);
            String type = extra.substring(2, 6);
            String weigh = extra.substring(extra.length() - 4);

            Integer iWeigh = Integer.parseInt(weigh, 16);
            Log.d(TAG, "十六进制重量: " + weigh + " 转换后的十进制重量: " + iWeigh);
            BigDecimal dWeigh = new BigDecimal(iWeigh).divide(new BigDecimal( "100"));
//            Float dWeigh = (float)iWeigh / 100;
            Log.d(TAG, "计算后的公斤数: " + dWeigh);
            HomeActivity.setWeigh(source, type, dWeigh);
            if (WeighTypeEnum.PREFIX_WEIGH.getCode().equals(type)){
                // 开门前置称重
                EventBus.getDefault().post(ActionResultEnum.PREFIX_WEIGH_SUCCESS);
            }else if (WeighTypeEnum.SUFFIX_WEIGH.getCode().equals(type)){
                // 关门后置称重
                EventBus.getDefault().post(ActionResultEnum.SUFFIX_WEIGH_SUCCESS);
            }
        }else {
            // 称重失败 前置称重失败，还是后置称重失败
            // 前置称重失败，当前界面应该卡在开门中
            // 后置称重失败，当前界面应该卡在
            // TODO: 2019/1/9 待定
            EventBus.getDefault().post(new MessageEvent(null, "称重模块异常，请联系管理员!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
    }

    private static void startRecycleComplteConfirmDelayTask(Handler handler) {
        clearTask(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK, handler);
        PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK = getRecycleCompleteConfirmDelayTask();
        handler.postDelayed(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK, PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TIME);
    }

    private static void clearTask(Runnable task, Handler myHandler){
        if (task != null){
            myHandler.removeCallbacks(task);
            task = null;
        }
    }

    private static Runnable getRecycleCompleteConfirmDelayTask() {
        return new Runnable() {
            @Override
            public void run() {
                // 收到光电 2 触发后，计时 2 秒，如果还是没有收到光电 3 信号，做结算处理！
                EventBus.getDefault().post(new MessageEvent("",SCAN_VALIDATE_SUCCESS_DONT_RECEIVE_COMPLATE_SINGAL.getCode(), MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
            }
        };
    }
}
