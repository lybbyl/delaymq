package com.luoluo.delaymq.consumer;

import lombok.Getter;

/**
 * 消息消费状态
 *
 * @Date: 2020/7/9 18:04
 * @Author: luoluo
 * @Description:
 */
public enum ConsumerStatus {
    /**
     * 消费成功
     */
    SUCCESS("SUCCESS"),
    /**
     * 消费失败
     */
    FAIL("FAIL"),
    /**
     * 完全失败
     */
    COMPLETE_FAIL("COMPLETE_FAIL"),
    ;

    ConsumerStatus(String description) {
        this.description = description;
    }

    @Getter
    private String description;
}
