package com.luoluo.delaymq.consumer;

/**
 * 预留接口 以防扩展
 * @param <T>
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public interface DelayMQConsumerLifecycleListener<T> {

    void prepareStart(final T consumer);
}
