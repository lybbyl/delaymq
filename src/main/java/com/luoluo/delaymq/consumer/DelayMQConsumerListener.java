package com.luoluo.delaymq.consumer;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回调监听接口
 *
 * @param <T>
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public interface DelayMQConsumerListener<T> {

    /**
     * 消息回调
     */
    ConsumerStatus onMessage(T message, String msgId);

    /**
     * 事务回滚
     */
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.NESTED)
    default ConsumerStatus onTransactionMessage(T message, String msgId) {
        return onMessage(message, msgId);
    }

    /**
     * 扩展接口 消费成功回调
     */
    void extraOperationAfterMessageSuccess(T message, String msgId);

    /**
     * 扩展接口 消费失败回调
     */
    void extraOperationAfterMessageFail(T message, String msgId);

    /**
     * 扩展接口 消费完全失败回调
     */
    void extraOperationAfterMessageCompleteFail(T message, String msgId);

}
