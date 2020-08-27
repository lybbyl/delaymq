package com.luoluo.sample.message;

/**
 * 示例 01 的 Message 消息
 */
public class DemoTestMessage {

    public static final String TOPIC = "DEMO_01";

   private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "DemoTestMessage{" +
                "name=" + name +
                '}';
    }

}
