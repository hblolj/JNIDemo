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

    /**
     * 塑料回收门开启结果延时任务
     */
    private static Runnable PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK = null;
    /**
     * 塑料回收门延时关闭任务
     */
    private static Runnable PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK = null;

    private static final Long PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TIME = 5000L;
    /**
     * 塑料回收们延时关闭时间
     */
    private static final Long PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TIME = 30000L;

    public static void handlerReceiveData(String receiveData, Handler myHandler){

        // 1. 判断是否是应答指令 waitOrder 中查询
        OrderValidate replyValidate = SerialHelper.waitReplys.get(receiveData);
        if (replyValidate != null){
            SerialHelper.waitReplys.remove(receiveData);
            handleReplyOrder(replyValidate, myHandler);
            return;
        }

        // 2. 判断是否是执行结果 解析出 SourceAddress + ActionCode -> waitResult 中查询
        Order order = OrderAnalyzeUtil.analyzeOrder(receiveData);
        if (order == null){
            Log.d(TAG, "handlerReceiveData - 指令解析异常: " + receiveData);
            return;
        }

        String key = order.getSourceAddress() + order.getActionCode();
        if (SerialHelper.waitResults.containsKey(key)){
            // 执行结果返回
            // TODO: 2018/12/20 根据具体的场景，做对应的业务处理
            // TODO: 2018/12/20 收到执行结果，根据业务场景，成功 or 失败 是否开启信号监听延时
            SerialHelper.waitResults.remove(key);
            return;
        }

        // 3. 判断是否是监听信号 解析出 SourceAddress + ActionCode -> waitSignal 中查询
        if (SerialHelper.waitSignal.containsKey(key)){
            // 检测信号
            // TODO: 2018/12/20 根据具体的场景，做对应的业务处理
            SerialHelper.waitSignal.remove(key);
            return;
        }
    }

    /**
     * 处理指令应答
     * @param v
     */
    public static void handleReplyOrder(OrderValidate v, Handler myHandler){

        Order replyOrder = v.getWaitReceiverOrder();

        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.OPEN_USER_RECYCLE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 1. 塑料瓶回收门 - 开门指令，收到应答后，开启一个延时任务(x 秒内没有收到对应的开门结果，做开门失败处理)
            if (PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK != null){
                // 收到开门指令应答时，发现已经存在监听开门结果延时任务
                // 1. 终止该任务
                myHandler.removeCallbacks(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK);
                // 2. 置为 null
                PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK = null;
            }

            // 重置延时校验开门结果任务
            PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK = getPlasticRecycleOpenDoorValidateResultDelayTask();
            myHandler.postDelayed(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK, PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TIME);
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 2. 塑料瓶回收门 - 关门指令，收到应发后，开启一个延时任务(x 秒内没有收到对应的开门结果，做错误处理) -> IC 没发或者发了，没收到
        }

    }

    /**
     * 处理指令执行结果
     * @param v
     */
    public static void handleResultOrder(OrderValidate v, Handler myHandler){

        Order replyOrder = v.getWaitReceiverOrder();

        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.OPEN_USER_RECYCLE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 塑料瓶回收门 - 开门结果，如果开门成功，开启一个延时关门任务(30秒)
            if (PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK != null){
                // 1. 收到开门指令，关闭延时检测开门结果任务
                myHandler.removeCallbacks(PLASTIC_RECYCLE_OPEN_DOOR_VALIDATE_RESULT_DELAY_TASK);
            }

            // 2. 判断开门是否成功
            // 判断开门是否成功
            if ("FFFF".equals(replyOrder.getParam().toUpperCase())){
                // TODO: 2018/12/22 界面提示，箱门已打开，请投递
                // TODO: 2018/12/22 清空之前条码扫描存储数据
                // 开门成功, 开启一个延时关门任务
                if (PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK != null){
                    // 已经存在一个延时关门任务
                    // 移除
                    myHandler.removeCallbacks(PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK);
                    PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK = null;
                }

                // TODO: 2018/12/22 界面上同时开启倒计时 CountDownTimer
                // 重置延时关门任务
                PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK = getPlasticRecycleDoorDelayCloseTask();
                myHandler.postDelayed(PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TASK, PLASTIC_RECYCLE_DOOR_DELAY_CLOSE_TIME);
            }else {
                // 开门失败
                // TODO: 2018/12/22 界面提示，开门失败
            }
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.CLOSE_USER_RECYCLE_RESULT_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 2. 塑料瓶回收门 - 关门结果，如果成功 -> 判断关门上下文环境，不是所有关门都触发下列操作
        }
    }

    /**
     * 处理信号检测结果
     * @param v
     */
    public static void handleSignalOrder(OrderValidate v){
        Order replyOrder = v.getWaitReceiverOrder();
        if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.REFRESH_DOOR_CLOESE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 1. 塑料瓶回收 光电 1 信号，刷新塑料瓶回收门延时关门任务时间(30秒重置)
            // TODO: 2018/12/22 重置 Timer 倒计时
        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.BAR_CODE_SCAN_VALIDATE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 2. 条码扫描结果校验请求(校验光电 2 感应器触发)

        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.COMMUNICATION_EXCEPTION_NOTICE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 3. 通信异常通知(延迟时间后，IC 未收到扫码结果)

        }else if (ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS.equals(replyOrder.getSourceAddress()) &&
                ComConstant.RECYCLE_RESULT_NOTICE_ACTION_CODE.equals(replyOrder.getActionCode())){
            // 4. 物品投递结果通知
            // 光电 3 感应器触发
            // ​扫码失败，用户取回物品触发
            // 扫码成功，延迟时间后，光电 3 感应器未触发​​
            // .......
        }
    }

    private static Runnable getPlasticRecycleOpenDoorValidateResultDelayTask(){
        return new Runnable() {
            @Override
            public void run() {
                // 收到开门指令应答后，计时 5 秒，如果还是没有收到开门结果，做开门失败处理！
                // TODO: 2018/12/22 失败处理
                EventBus.getDefault().post(new MessageEvent("开门失败！哇哈哈哈哈哈!", MessageEvent.MESSAGE_TYPE_NOTICE));
            }
        };
    }

    private static Runnable getPlasticRecycleDoorDelayCloseTask() {
        return new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/12/22 延时关门任务
                // 1. 发送关门指令
                // 2. 自动结算回收结果
            }
        };
    }
}
