package com.example.ori.jnidemo.bean;

public class MessageEvent {

    public static final Integer MESSAGE_TYPE_NOTICE = 1;
    public static final Integer MESSAGE_TYPE_RECEIVER_VIEW = 2;
    public static final Integer MESSAGE_TYPE_SEND_VIEW = 3;
    public static final Integer MESSAGE_TYPE_LOG_VIEW = 4;

    private String message;

    private Integer type;

    public MessageEvent() {
    }

    public MessageEvent(String message) {
        this.message = message;
    }

    public MessageEvent(String message, Integer type) {
        this.message = message;
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
