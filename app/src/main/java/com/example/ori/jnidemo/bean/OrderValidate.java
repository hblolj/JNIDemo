package com.example.ori.jnidemo.bean;

public class OrderValidate {

    private Order sendOrder;

    private Order waitReceiverOrder;

    private Integer retryCount;

    public OrderValidate() {
    }

    public OrderValidate(Order sendOrder, Order waitReceiverOrder, Integer retryCount) {
        this.sendOrder = sendOrder;
        this.waitReceiverOrder = waitReceiverOrder;
        this.retryCount = retryCount;
    }

    public Order getSendOrder() {
        return sendOrder;
    }

    public void setSendOrder(Order sendOrder) {
        this.sendOrder = sendOrder;
    }

    public Order getWaitReceiverOrder() {
        return waitReceiverOrder;
    }

    public void setWaitReceiverOrder(Order waitReceiverOrder) {
        this.waitReceiverOrder = waitReceiverOrder;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
