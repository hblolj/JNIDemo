package com.example.ori.jnidemo.utils;

import android.os.Handler;
import android.util.Log;

import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

/**
 * @author: hblolj
 * @date: 2018/12/24 9:30
 * @description:
 */
public class OrderUtils {

    private static final String TAG = OrderUtils.class.getSimpleName();

    public static void retrySendOrder(SerialHelper comHelper, OrderValidate validate, Handler myHandler){
        validate.setRetryCount(validate.getRetryCount() + 1);
        SerialHelper.waitReplys.put(validate.getWaitReceiverOrder().getOrderContent(), validate);
        Log.d(TAG, "第" + validate.getRetryCount() + "次指令发送，指令内容: " + validate.getSendOrder().getOrderContent());
        comHelper.sendHex(validate.getSendOrder(), validate.getWaitReceiverOrder().getOrderContent(), myHandler);
    }

    public static void sendOrder(SerialHelper comHelper, String targetAddress, String actionCode, String param, Handler myHandler, Integer retryCount){

        Boolean empty = StringUtil.isEmpty(param);

        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, targetAddress, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);
        Order replyOrder = new Order(targetAddress, ComConstant.ANDROID_ADDRESS, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);

        SerialHelper.waitReplys.put(replyOrder.getOrderContent(), new OrderValidate(sendOrder, replyOrder, retryCount));
        Log.d(TAG, "第" + retryCount + "次指令发送，指令内容: " + sendOrder.getOrderContent());
        comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
    }

    public static void sendNeedResponseOrder(SerialHelper comHelper, String targetAddress, String actionCode, String param, Handler myHandler, Integer retryCount){

        Boolean empty = StringUtil.isEmpty(param);

        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, targetAddress, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);
        Order replyOrder = new Order(targetAddress, ComConstant.ANDROID_ADDRESS, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);

        // 操作码 转 10进制 + 1，然后转回 16进制
        String resultAction = CommonUtil.hexAdd(actionCode, 1);
        String key = targetAddress + resultAction;

        SerialHelper.waitReplys.put(replyOrder.getOrderContent(), new OrderValidate(sendOrder, replyOrder, retryCount));
        SerialHelper.waitResults.put(key, sendOrder);
        Log.d(TAG, "第" + retryCount + "次指令发送，指令内容: " + sendOrder.getOrderContent());
        comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
    }
}
