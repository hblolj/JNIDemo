package com.example.ori.jnidemo.utils;

import android.os.Handler;
import android.util.Log;

import com.example.ori.jnidemo.bean.ActionMessageEvent;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

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
        try {
            comHelper.sendHex(validate.getSendOrder(), validate.getWaitReceiverOrder().getOrderContent(), myHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 数据发送异常，移除存储的校验对象
            SerialHelper.waitReplys.remove(validate.getWaitReceiverOrder().getOrderContent());
            EventBus.getDefault().post(new MessageEvent("消息发送异常!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
    }

    public static void sendOrder(SerialHelper comHelper, String targetAddress, String actionCode, String param, Handler myHandler, Integer retryCount){

        Boolean empty = StringUtil.isEmpty(param);

        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, targetAddress, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);
        Order replyOrder = new Order(targetAddress, ComConstant.ANDROID_ADDRESS, actionCode, empty ? Order.DEFAULT_ORDER_PARAM : param);

        SerialHelper.waitReplys.put(replyOrder.getOrderContent(), new OrderValidate(sendOrder, replyOrder, retryCount));
        Log.d(TAG, "第" + retryCount + "次指令发送，指令内容: " + sendOrder.getOrderContent());
        try {
            comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 数据发送异常，移除存储的校验对象
            SerialHelper.waitReplys.remove(replyOrder.getOrderContent());
            EventBus.getDefault().post(new MessageEvent("消息发送异常!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
    }

    public static void sendOrder(SerialHelper comHelper, ActionMessageEvent event, Handler myHandler){

        Boolean empty = StringUtil.isEmpty(event.getParam());

        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, event.getTargetAddress(), event.getActionCode(), empty ? Order.DEFAULT_ORDER_PARAM : event.getParam());
        Order replyOrder = new Order(event.getTargetAddress(), ComConstant.ANDROID_ADDRESS, event.getActionCode(), empty ? Order.DEFAULT_ORDER_PARAM : event.getParam());

        SerialHelper.waitReplys.put(replyOrder.getOrderContent(), new OrderValidate(sendOrder, replyOrder, event.getRetryCount()));
        Log.d(TAG, "第" + event.getRetryCount() + "次指令发送，指令内容: " + sendOrder.getOrderContent());
        try {
            comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 数据发送异常，移除存储的校验对象
            SerialHelper.waitReplys.remove(replyOrder.getOrderContent());
            EventBus.getDefault().post(new MessageEvent("消息发送异常!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
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
        try {
            comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 数据发送异常，移除存储的校验对象
            SerialHelper.waitReplys.remove(replyOrder.getOrderContent());
            SerialHelper.waitResults.remove(key);
            EventBus.getDefault().post(new MessageEvent("消息发送异常!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
    }

    public static void sendNeedResponseOrder(SerialHelper comHelper, ActionMessageEvent event, Handler myHandler){

        Boolean empty = StringUtil.isEmpty(event.getParam());

        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, event.getTargetAddress(), event.getActionCode(), empty ? Order.DEFAULT_ORDER_PARAM : event.getParam());
        Order replyOrder = new Order(event.getTargetAddress(), ComConstant.ANDROID_ADDRESS, event.getActionCode(), empty ? Order.DEFAULT_ORDER_PARAM : event.getParam());

        // 操作码 转 10进制 + 1，然后转回 16进制
        String resultAction = CommonUtil.hexAdd(event.getActionCode(), 1);
        String key = event.getTargetAddress() + resultAction;

        SerialHelper.waitReplys.put(replyOrder.getOrderContent(), new OrderValidate(sendOrder, replyOrder, event.getRetryCount()));
        SerialHelper.waitResults.put(key, sendOrder);
        Log.d(TAG, "第" + event.getRetryCount() + "次指令发送，指令内容: " + sendOrder.getOrderContent());
        try {
            comHelper.sendHex(sendOrder, replyOrder.getOrderContent(), myHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 数据发送异常，移除存储的校验对象
            SerialHelper.waitReplys.remove(replyOrder.getOrderContent());
            SerialHelper.waitResults.remove(key);
            EventBus.getDefault().post(new MessageEvent("消息发送异常!", MessageEvent.MESSAGE_TYPE_NOTICE));
        }
    }
}
