package com.example.ori.jnidemo.utils;

import android.util.Log;

import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

/**
 * @author: hblolj
 * @date: 2018/12/20 16:10
 * @description: 指令处理工具类
 */
public class OrderHandleUtil {

    private static final String TAG = OrderHandleUtil.class.getSimpleName();

    public static void handlerReceiveData(String receiveData){

        // 1. 判断是否是应答指令 waitOrder 中查询
        OrderValidate replyValidate = SerialHelper.waitReplys.get(receiveData);
        if (replyValidate != null){
            SerialHelper.waitReplys.remove(receiveData);
            handleReplyOrder(replyValidate);
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
    public static void handleReplyOrder(OrderValidate v){
        Order sendOrder = v.getSendOrder();
        Order replyOrder = v.getWaitReceiverOrder();
        // TODO: 2018/12/22 应答指令规则 地址互转，操作码不变
        // TODO: 2018/12/22 地址 + ActionCode 判断具体业务
        String sa = replyOrder.getSourceAddress();
        String actionCode = replyOrder.getActionCode();
        // 1. 塑料瓶回收门 - 开门指令，收到应答后，开启一个延时任务(x 秒内没有收到对应的开门结果，做错误处理) -> IC 没发或者发了，没收到
        // 2. 塑料瓶回收门 - 关门指令，收到应发后，开启一个延时任务(x 秒内没有收到对应的开门结果，做错误处理) -> IC 没发或者发了，没收到
        // 3. 塑料瓶回收 - 扫码结果，收到应答后，ToBeContinue ->
    }

    /**
     * 处理指令执行结果
     * @param v
     */
    public static void handleResultOrder(OrderValidate v){
        Order sendOrder = v.getSendOrder();
        Order replyOrder = v.getWaitReceiverOrder();
        // 1. 塑料瓶回收门 - 开门结果，如果开门成功，开启一个延时关门任务(30秒)
        // 2. 塑料瓶回收门 - 关门结果，如果成功 -> 判断关门上下文环境，不是所有关门都触发下列操作
        // 3. 塑料瓶回收 - 扫码结果反馈
    }

    /**
     * 处理信号检测结果
     * @param v
     */
    public static void handleSignalOrder(OrderValidate v){
        Order sendOrder = v.getSendOrder();
        Order replyOrder = v.getWaitReceiverOrder();
        // 1. 塑料瓶回收 光电 1 信号，刷新塑料瓶回收门延时关门任务时间(30秒重置)
        // 2. 塑料瓶回收 光电 2 信号 -> 检测扫码结果 -> 判断扫码是否成功
        // 3. 塑料瓶回收 光电 3 信号 -> 回收确认 -> 找到对应的条码编号，标记状态 -> 显示已投递 X 个
    }

    private static void matchBiz(String sourceAddress, String actionCode){
        switch (sourceAddress){
            case ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS:
                //塑料瓶回收 按 Action Code 判断业务

                break;
            case ComConstant.METAL_RECYCLE_IC_ADDRESS:
                // 金属回收
                break;
            case ComConstant.PAPER_RECYCLE_IC_ADDRESS:
                // 纸类回收
                break;
            default:
                break;
        }
    }
}
