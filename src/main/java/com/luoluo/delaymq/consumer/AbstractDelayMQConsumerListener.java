package com.luoluo.delaymq.consumer;

/**
 * @ClassName AbstractDelayMQConsumer
 * @Description: 消费者提供默认实现（适配器模式 默认适配器）
 * @Author luoluo
 * @Date 2020/7/20
 * @Version V1.0
 **/
public abstract class AbstractDelayMQConsumerListener<T> implements DelayMQConsumerListener<T> {

    @Override
    public void extraOperationAfterMessageSuccess(T message, String msgId) {

    }

    @Override
    public void extraOperationAfterMessageFail(T message, String msgId) {

    }

    @Override
    public void extraOperationAfterMessageCompleteFail(T message, String msgId) {

    }
}
