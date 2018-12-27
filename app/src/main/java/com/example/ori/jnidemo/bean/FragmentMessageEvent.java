package com.example.ori.jnidemo.bean;

/**
 * @author: hblolj
 * @date: 2018/12/27 16:16
 * @description: Fragment 之间传递数据的 Bean
 */
public class FragmentMessageEvent {

    public static final Integer SWITCH_FRAGMENT = 1;

    private Integer messageType;

    private Object data;

    private Object extra;

    public FragmentMessageEvent() {
    }

    public FragmentMessageEvent(Integer messageType, Object data, Object extra) {
        this.messageType = messageType;
        this.data = data;
        this.extra = extra;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
