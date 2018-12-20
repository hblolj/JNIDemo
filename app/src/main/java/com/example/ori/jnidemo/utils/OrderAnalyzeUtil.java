package com.example.ori.jnidemo.utils;

import com.example.ori.jnidemo.bean.Order;

/**
 * @author: hblolj
 * @date: 2018/12/20 16:09
 * @description: 指令解析工具类
 */
public class OrderAnalyzeUtil {

    /**
     * 将字符串 Order 转为对象 Order
     * @param sOrder
     * @return
     */
    public static Order analyzeOrder(String sOrder){

        Order order = null;

        if (StringUtil.isEmpty(sOrder)){
            return null;
        }

        if (sOrder.indexOf(Order.DEFAULT_ORDER_PREFIX) == 0 && Order.DEFAULT_ORDER_LENGTH == sOrder.length()){
            // 合法的指令格式

            Integer oldIndex = 0;
            Integer currentIndex = Order.DEFAULT_PREFIX_LENGTH;
            order = new Order();

            // 前缀 4 0-4
            order.setPrefix(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_CORE_LENGTH;

            // 长度 2 4-6
            order.setLength(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_SOURCE_ADDRESS_LENGTH;

            // 指令发送方地址 2 6-8
            order.setSourceAddress(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_TARGET_ADDRESS_LENGTH;

            // 指令接收方地址 2 8-10
            order.setTargetAddress(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_ACTION_CODE_LENGTH;

            // 操作码 2 10-12
            order.setActionCode(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_PARAM_LENGTH;

            // 数据包 4 12-16
            order.setParam(sOrder.substring(oldIndex, currentIndex));
            oldIndex = currentIndex;
            currentIndex = currentIndex + Order.DEFAULT_CRC_LENGTH;

            // 校验码 4 16-20
            order.setCrc(sOrder.substring(oldIndex, currentIndex));
        }

        return order;
    }
}
