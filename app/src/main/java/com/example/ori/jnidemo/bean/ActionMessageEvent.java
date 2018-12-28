package com.example.ori.jnidemo.bean;

/**
 * @author: hblolj
 * @date: 2018/12/28 9:45
 * @description: 指令操作消息事件 Bean
 */
public class ActionMessageEvent {

    private String targetAddress;

    private String actionCode;

    private String param;

    private Integer retryCount;

    private Boolean needResult;

    public ActionMessageEvent() {
    }

    public ActionMessageEvent(String targetAddress, String actionCode, String param, Integer retryCount, Boolean needResult) {
        this.targetAddress = targetAddress;
        this.actionCode = actionCode;
        this.param = param;
        this.retryCount = retryCount;
        this.needResult = needResult;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Boolean getNeedResult() {
        return needResult;
    }

    public void setNeedResult(Boolean needResult) {
        this.needResult = needResult;
    }
}
