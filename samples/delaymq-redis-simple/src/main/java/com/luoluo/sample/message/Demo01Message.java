package com.luoluo.sample.message;

/**
 * 示例 01 的 Message 消息
 */
public class Demo01Message {

    public static final String TOPIC = "DEMO_01";

    /**
     * 编号
     */
    private String id;

    private int transactionValue;

    public Demo01Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setTransactionValue(int transactionValue) {
        this.transactionValue = transactionValue;
    }

    public int getTransactionValue() {
        return transactionValue;
    }

}
