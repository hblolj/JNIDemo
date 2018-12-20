package com.example.ori.jnidemo.utils;

import android.util.Log;

import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

/**
 * @author: hblolj
 * @date: 2018/12/20 16:10
 * @description: 指令处理工具类
 */
public class OrderHandleUtil {

    public static final String TAG = OrderHandleUtil.class.getSimpleName();

    public static void handlerReceiveData(String receiveData){

        // 1. 判断是否是应答指令 waitOrder 中查询
        if (SerialHelper.waitOrders.containsKey(receiveData)){
            SerialHelper.waitOrders.remove(receiveData);
            // TODO: 2018/12/20 收到应答后，根据实际场景决定是否要做执行结果延时
//            SerialHelper.waitResults.put();
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
}
