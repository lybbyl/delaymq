package com.luoluo.delaymq.producer.annotation;

import com.luoluo.delaymq.producer.DelayMQLocalTransactionListener;

/**
 * @ClassName AbstractDelayMQConsumer
 * @Description: 事务消息扩展提供默认实现（适配器模式 默认适配器）
 * @Author luoluo
 * @Date 2020/7/20
 * @Version V1.0
 **/
public abstract class AbstractDelayMQTransactionListener<T> implements DelayMQLocalTransactionListener<T> {

    @Override
    public void afterCommitTransactionMessage(final T t,String msgId) {

    }

    @Override
    public void afterRollbackTransactionMessage(final T t,String msgId) {

    }

    @Override
    public void afterOverRetryTransactionMessage(final T t,String msgId) {

    }

    @Override
    public void afterDelayTransactionMessage(final T t,String msgId) {

    }
}
