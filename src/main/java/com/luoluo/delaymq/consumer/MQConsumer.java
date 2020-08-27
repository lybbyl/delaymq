package com.luoluo.delaymq.consumer;

/**
 * @Date: 2020/7/8 14:56
 * @Author: luoluo
 * @Description: MQ消费者 被定时调度
 */
public interface MQConsumer extends Runnable {

    /**
     * 初始化
     */
    void initialize(DelayMQConsumerListener delayMQConsumerListener);

}
