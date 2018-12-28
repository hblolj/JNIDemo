package com.example.ori.jnidemo.utils;

import android.os.Handler;
import android.util.Log;

import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

import org.greenrobot.eventbus.EventBus;

/**
 * @author: hblolj
 * @date: 2018/12/20 16:10
 * @description: 指令处理工具类
 */
public class OrderHandleUtil {

    private static final String TAG = OrderHandleUtil.class.getSimpleName();

    // 塑料瓶回收 - 开门 - 应答
    private static final Integer PLASTIC_BOTTLE_RECYCLE_OPEN_DOOR_REPLY = 1;
    // 塑料瓶回收 - 关门 - 应答
    private static final Integer PLASTIC_BOTTLE_RECYCLE_CLOSE_DOOR_REPLY = 2;
    // 塑料瓶回收 - 扫码结果校验 - 应答
    private static final Integer PLASTIC_BOTTLE_RECYCLE_SCAN_VALIDATE_REPLY = 3;
    // 塑料瓶回收 - 强制回收 - 应答
    private static final Integer PLASTIC_BOTTLE_RECYCLE_FORCE_RECYCLE_REPLY = 4;

    /**
     * 塑料回收门开启结果延时任务
     */
    private static Runnable PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK = null;

    private static final Long PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TIME = 10000L;

    /**
     * 塑料回收门关闭结果延时任务
     */
    private static Runnable PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TASK = null;

    private static final Long PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TIME = 5000L;

    public static void handlerReceiveData(String receiveData, Handler myHandler){

        Log.d(TAG, "收到的串口数据: " + receiveData);

        // 1. 判断是否是应答指令 waitOrder 中查询
        OrderValidate replyValidate = SerialHelper.waitReplys.get(receiveData);
        if (replyValidate != null){
            SerialHelper.waitReplys.remove(receiveData);
            handleReplyOrder(replyValidate.getWaitReceiverOrder(), myHandler);
            return;
        }

        // 2. 判断是否是执行结果 解析出 SourceAddress + ActionCode -> waitResult 中查询
        Order receiveOrder = OrderAnalyzeUtil.analyzeOrder(receiveData);
        if (receiveOrder == null){
            Log.d(TAG, "handlerReceiveData - 指令解析异常: " + receiveData);
            return;
        }

        String rKey = receiveOrder.getSourceAddress() + receiveOrder.getActionCode();
        Order sendOrder = SerialHelper.waitResults.get(rKey);
        if (sendOrder != null){
            // 执行结果返回
            SerialHelper.waitResults.remove(rKey);
            handleResultOrder(receiveOrder, myHandler);
            return;
        }

        // 3. 判断是否是监听信号 解析出 SourceAddress + ActionCode -> waitSignal 中查询
        handleSignalOrder(receiveOrder);
    }

    /**
     * 未收到应答处理
     * @param replyOrder
     * @param myHandler
     */
    public static void handleTimeOutReplyOrder(Order replyOrder, Handler myHandler){

        Integer replyOrderType = getReplyOrderType(replyOrder.getSourceAddress(), replyOrder.getActionCode());
        if (replyOrderType == null){
            Log.d(TAG, "handleReplyOrder - 未知的应答指令: " + replyOrder.getOrderContent());
            return;
        }

        if (PLASTIC_BOTTLE_RECYCLE_OPEN_DOOR_REPLY.equals(replyOrderType)){
            // 塑料瓶回收 - 开门 - 应答未收到
            String key = replyOrder.getSourceAddress() + CommonUtil.hexAdd(replyOrder.getActionCode(), 1);
            EventBus.getDefault().post(new MessageEvent(key, MessageEvent.MESSAGE_TYPE_OPEN_DOOR_FAILD));
        }else if (PLASTIC_BOTTLE_RECYCLE_CLOSE_DOOR_REPLY.equals(replyOrderType)){
            // 塑料瓶回收 - 关门 - 应答未收到
            String key = replyOrder.getSourceAddress() + CommonUtil.hexAdd(replyOrder.getActionCode(), 1);
            EventBus.getDefault().post(new MessageEvent(key, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_FAILD));
        }else if (PLASTIC_BOTTLE_RECYCLE_SCAN_VALIDATE_REPLY.equals(replyOrderType)){
            // 扫码结果 - 应答未收到
            Log.d(TAG, "扫码结果反馈指令应答未收到!");
        }else if (PLASTIC_BOTTLE_RECYCLE_FORCE_RECYCLE_REPLY.equals(replyOrderType)){
            // 强制回收指令 - 应答未收到
            Log.d(TAG, "强制回收指令应答未收到!");
        }
    }

    /**
     * 处理指令应答
     * @param replyOrder
     */
    public static void handleReplyOrder(Order replyOrder, Handler myHandler){

        Integer replyOrderType = getReplyOrderType(replyOrder.getSourceAddress(), replyOrder.getActionCode());
        if (replyOrderType == null){
            Log.d(TAG, "未知的应答指令: " + replyOrder.getOrderContent());
            return;
        }

        if (PLASTIC_BOTTLE_RECYCLE_OPEN_DOOR_REPLY.equals(replyOrderType)){

            Log.d(TAG, "收到开门响应指令");
            // 塑料瓶回收开门指令应答, 收到开门指令应答时，如果已经存在监听开门结果延时任务, 终止该任务
            resetTask(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK, myHandler);

            // 重置延时校验开门结果任务(x 秒内没有收到对应的开门结果，做开门失败处理)
            PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK = getPlasticRecycleOpenDoorValidateResultDelayTask(replyOrder);

            myHandler.postDelayed(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK, PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TIME);

        }else if (PLASTIC_BOTTLE_RECYCLE_CLOSE_DOOR_REPLY.equals(replyOrderType)){

            Log.d(TAG, "收到关门响应指令");
            // 塑料瓶回收门 - 关门指令，收到应发后，开启一个延时任务(x 秒内没有收到对应的开门结果，做错误处理) -> IC 没发或者发了，没收到
            resetTask(PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TASK, myHandler);

            // 重置延时校验关门结果任务(x 秒内没有收到对应的关门结果，做关门失败处理)
            PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TASK = getPlasticRecycleCloseDoorValidateResultDelayTask(replyOrder);

            myHandler.postDelayed(PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TASK, PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TIME);

        }else if (PLASTIC_BOTTLE_RECYCLE_SCAN_VALIDATE_REPLY.equals(replyOrderType)){
            // 3. 扫码结果反馈应答
            Log.d(TAG, "扫码结果反馈指令响应!");
        }else if (PLASTIC_BOTTLE_RECYCLE_FORCE_RECYCLE_REPLY.equals(replyOrderType)){
            // 4. 强制回收指令应答
            Log.d(TAG, "强制回收指令响应!");
        }
    }

    /**
     * 处理指令执行结果
     * @param replyOrder
     */
    public static void handleResultOrder(Order replyOrder, Handler myHandler){

        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.OPEN_USER_RECYCLE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){

            // 塑料瓶回收门 - 开门结果, 收到开门指令，关闭延时检测开门结果任务
            removeTask(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK, myHandler);

            // 判断开门是否成功
            String key = replyOrder.getSourceAddress() + replyOrder.getActionCode();
            if ("FFFF".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到开门成功指令!");
                EventBus.getDefault().post(new MessageEvent(key, true, MessageEvent.MESSAGE_TYPE_OPEN_DOOR_RESULT));
            }else if ("0000".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到开门失败指令!");
                EventBus.getDefault().post(new MessageEvent(key, false, MessageEvent.MESSAGE_TYPE_OPEN_DOOR_RESULT));
            }

        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.CLOSE_USER_RECYCLE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){

            // 塑料瓶回收门 - 关门结果，如果成功 -> 判断关门上下文环境，不是所有关门都触发下列操作
            removeTask(PLASTIC_RECYCLE_CLOSE_DOOR_VALIDATE_RESULT_DELAY_TASK, myHandler);

            String key = replyOrder.getSourceAddress() + replyOrder.getActionCode();
            if ("FFFF".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到关门成功指令!");
                EventBus.getDefault().post(new MessageEvent(key, true, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT, true));
            }else if ("0000".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到关门失败指令!");
                EventBus.getDefault().post(new MessageEvent(key, false, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT, true));
            }else if ("EEEE".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到强制回收前置关门成功指令!");
                EventBus.getDefault().post(new MessageEvent(key, true, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT, false));
            }else if ("1111".equals(replyOrder.getParam().toUpperCase())){
                Log.d(TAG, "收到强制回收前置关门失败指令!");
                EventBus.getDefault().post(new MessageEvent(key, false, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT, false));
            }

        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 3. 扫码结果反馈结果 ???
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.FORCE_RECYCLE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 4. 强制回收回收结果
            String key = replyOrder.getSourceAddress() + replyOrder.getActionCode();
            if ("FFFF".equals(replyOrder.getParam().toUpperCase())) {
                Log.d(TAG, "收到强制回收成功指令!");
                EventBus.getDefault().post(new MessageEvent(key, true, MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_RESULT));
            }else {
                Log.d(TAG, "收到强制回收失败指令!");
                EventBus.getDefault().post(new MessageEvent(key, false, MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_RESULT));
            }
        }
    }

    /**
     * 处理信号检测结果
     * @param replyOrder
     */
    public static void handleSignalOrder(Order replyOrder){
        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.REFRESH_DOOR_CLOESE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 1. 塑料瓶回收 光电 1 信号，刷新塑料瓶回收门延时关门任务时间(30秒重置)
            EventBus.getDefault().post(new MessageEvent("","重置关门倒计时!", MessageEvent.MESSAGE_TYPE_RESET_CLOSE_DOOR_COUNTDOWN));
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.BAR_CODE_SCAN_VALIDATE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 2. 条码扫描结果校验请求(校验光电 2 感应器触发)
            EventBus.getDefault().post(new MessageEvent("","请求获取扫码结果!", MessageEvent.MESSAGE_TYPE_REQUEST_SCAN_RESULT));
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.COMMUNICATION_EXCEPTION_NOTICE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 3. 通信异常通知(延迟时间后，IC 未收到扫码结果), 目前只作用于 IC 未收到扫码结果
            EventBus.getDefault().post(new MessageEvent("", "IC 未收到扫码结果!", MessageEvent.MESSAGE_TYPE_IC_NOT_RECEIVE_SCAN_RESULT));
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.RECYCLE_RESULT_NOTICE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 4. 单次物品投递结束
            EventBus.getDefault().post(new MessageEvent("", replyOrder.getParam(), MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
        }
    }

    private static void resetTask(Runnable task, Handler handler){
        if (task != null){
            handler.removeCallbacks(task);
            task = null;
        }
    }

    private static void removeTask(Runnable task, Handler handler){
        if (task != null){
            handler.removeCallbacks(task);
        }
    }

    private static Integer getReplyOrderType(String sourceAddress, String actionCode){
        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(sourceAddress) && ComConstant.OPEN_USER_RECYCLE_ACTION_CODE.equals(actionCode)){
            // 塑料瓶回收 - 开门 - 应答指令
            return PLASTIC_BOTTLE_RECYCLE_OPEN_DOOR_REPLY;
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(sourceAddress) && ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE.equals(actionCode)){
            // 塑料瓶回收 - 关门 - 应答指令
            return PLASTIC_BOTTLE_RECYCLE_CLOSE_DOOR_REPLY;
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(sourceAddress) && ComConstant.BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE.equals(actionCode)){
            // 塑料瓶回收 - 扫码结果 - 应答指令
            return PLASTIC_BOTTLE_RECYCLE_SCAN_VALIDATE_REPLY;
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(sourceAddress) && ComConstant.FORCE_RECYCLE_ACTION_CODE.equals(actionCode)){
            // 塑料瓶回收 - 强制回收 - 应答指令
            return PLASTIC_BOTTLE_RECYCLE_FORCE_RECYCLE_REPLY;
        }
        return null;
    }

    private static Runnable getPlasticRecycleOpenDoorValidateResultDelayTask(final Order replyOrder){
        return new Runnable() {
            @Override
            public void run() {
                // 收到开门指令应答后，计时 5 秒，如果还是没有收到开门结果，做开门失败处理！
                String key = replyOrder.getSourceAddress() + CommonUtil.hexAdd(replyOrder.getActionCode(), 1);
                EventBus.getDefault().post(new MessageEvent(key, MessageEvent.MESSAGE_TYPE_OPEN_DOOR_FAILD));
            }
        };
    }

    private static Runnable getPlasticRecycleCloseDoorValidateResultDelayTask(final Order replyOrder){
        return new Runnable() {
            @Override
            public void run() {
                // 收到关门指令应答后，计时 5 秒，如果还是没有收到关门结果，做关门失败处理！
                String key = replyOrder.getSourceAddress() + CommonUtil.hexAdd(replyOrder.getActionCode(), 1);
                EventBus.getDefault().post(new MessageEvent(key, MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_FAILD));
            }
        };
    }
}
