package com.example.ori.jnidemo.bean;

public class MessageEvent {

    public static final Integer MESSAGE_TYPE_NOTICE = 1;
    public static final Integer MESSAGE_TYPE_VIEW_NOTICE = 2;
    // 开门结果
    public static final Integer MESSAGE_TYPE_OPEN_DOOR_RESULT = 99;
    public static final Integer MESSAGE_TYPE_OPEN_DOOR_SUCCESS = 3;
    public static final Integer MESSAGE_TYPE_OPEN_DOOR_FAILD = 4;
    // 重置关门倒计时
    public static final Integer MESSAGE_TYPE_RESET_CLOSE_DOOR_COUNTDOWN = 5;
    // 光电 2 信号，请求扫码结果
    public static final Integer MESSAGE_TYPE_REQUEST_SCAN_RESULT = 6;
    public static final Integer MESSAGE_TYPE_IC_NOT_RECEIVE_SCAN_RESULT = 7;
    // 回收小结(单个回收物的结算)
    public static final Integer MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY = 8;
    // 关门结果
    public static final Integer MESSAGE_TYPE_CLOSE_DOOR_RESULT = 98;
    public static final Integer MESSAGE_TYPE_CLOSE_DOOR_SUCCESS = 9;
    public static final Integer MESSAGE_TYPE_CLOSE_DOOR_FAILD = 10;
    // 强制回收结果
    public static final Integer MESSAGE_TYPE_FORCE_RECYCLE_RESULT = 97;
    public static final Integer MESSAGE_TYPE_FORCE_RECYCLE_SUCCESS = 11;
    public static final Integer MESSAGE_TYPE_FORCE_RECYCLE_FAILD = 12;

    private String key;

    private Object message;

    private Integer type;

    private Object extra;

    public MessageEvent() {
    }

    public MessageEvent(Object message) {
        this.message = message;
    }

    public MessageEvent(String key, Integer type) {
        this.key = key;
        this.type = type;
    }

    public MessageEvent(String key, Object message, Integer type) {
        this.key = key;
        this.message = message;
        this.type = type;
    }

    public MessageEvent(String key, Object message, Integer type, Object extra) {
        this.key = key;
        this.message = message;
        this.type = type;
        this.extra = extra;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
