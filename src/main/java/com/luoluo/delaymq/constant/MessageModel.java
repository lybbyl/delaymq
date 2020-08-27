package com.luoluo.delaymq.constant;

/**
 * 消息广播集群消费
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public enum MessageModel {
    /**
     * 广播
     */
    BROADCASTING("BROADCASTING"),
    /**
     * 集群
     */
    CLUSTERING("CLUSTERING");

    private final String modeCN;

    MessageModel(String modeCN) {
        this.modeCN = modeCN;
    }

    public String getModeCN() {
        return this.modeCN;
    }
}
