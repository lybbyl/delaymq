package com.luoluo.delaymq.constant;

/**
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public enum ConsumeMode {
    /**
     *  消费者并发消费
     */
    CONCURRENTLY,

    /**
     * 消费者顺序消费
     */
    ORDERLY
}
