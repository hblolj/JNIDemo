package com.example.ori.jnidemo.bean;

import com.example.ori.jnidemo.utils.CommonUtil;
import com.example.ori.jnidemo.utils.StringUtil;
import com.example.ori.jnidemo.utils.crc.CRCUtils;

/**
 * @author: hblolj
 * @date: 2018/12/20 16:36
 * @description:
 */
public class Order {

    public static final Integer DEFAULT_PREFIX_LENGTH = 4;
    public static final Integer DEFAULT_CORE_LENGTH = 2;
    public static final Integer DEFAULT_SOURCE_ADDRESS_LENGTH = 2;
    public static final Integer DEFAULT_TARGET_ADDRESS_LENGTH = 2;
    public static final Integer DEFAULT_ACTION_CODE_LENGTH = 2;
    public static final Integer DEFAULT_PARAM_LENGTH = 4;
    public static final Integer DEFAULT_CRC_LENGTH = 4;

    public static final Integer DEFAULT_ORDER_LENGTH = DEFAULT_PREFIX_LENGTH + DEFAULT_CORE_LENGTH +
            DEFAULT_SOURCE_ADDRESS_LENGTH + DEFAULT_TARGET_ADDRESS_LENGTH + DEFAULT_ACTION_CODE_LENGTH +
            DEFAULT_PARAM_LENGTH + DEFAULT_CRC_LENGTH;

    public static final String DEFAULT_ORDER_PREFIX = "1919";
    public static final String DEFAULT_ORDER_PARAM = "0000";

    private String prefix;

    private String length;

    private String sourceAddress;

    private String targetAddress;

    private String actionCode;

    private String param;

    private String crc;

    public Order() {
    }

    public Order(String sourceAddress, String targetAddress, String actionCode, String param) {
        this.prefix = DEFAULT_ORDER_PREFIX;
        this.sourceAddress = sourceAddress.toUpperCase();
        this.targetAddress = targetAddress.toUpperCase();
        this.actionCode = actionCode.toUpperCase();
        this.param = param;
    }

    public Order(String prefix, String sourceAddress, String targetAddress, String actionCode, String param) {
        this.prefix = prefix.toUpperCase();
        this.sourceAddress = sourceAddress.toUpperCase();
        this.targetAddress = targetAddress.toUpperCase();
        this.actionCode = actionCode.toUpperCase();
        this.param = param;
    }

    private void calculateLength(){
        String core = sourceAddress + targetAddress + actionCode + param;
        Integer length = (core.length() + DEFAULT_CORE_LENGTH + DEFAULT_CRC_LENGTH) / 2;
        // 长度转16进制，补零
        this.length = CommonUtil.Ten2Hex(length);
    }

    private void calculateCRC(){
        if (StringUtil.isEmpty(length)){
            calculateLength();
        }
        String content = prefix + length + sourceAddress + targetAddress + actionCode + param;
        byte[] bytes = CommonUtil.toByteArray(content);
        this.crc = CRCUtils.getCRC(bytes);
    }

    public String getOrderContent(){
        if (!StringUtil.isNotEmpty(crc)){
            calculateCRC();
        }
        return prefix + length + sourceAddress + targetAddress + actionCode + param + crc;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLength() {
        if (StringUtil.isEmpty(length)){
            calculateLength();
        }
        return length;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getCrc() {
        if (StringUtil.isEmpty(crc)){
            calculateCRC();
        }
        return crc;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }
}
