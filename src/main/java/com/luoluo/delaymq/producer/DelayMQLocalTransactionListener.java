package com.luoluo.delaymq.producer;

/**
 * 事务回调监听
 *
 * @param <T>
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public interface DelayMQLocalTransactionListener<T> {

    /**
     * 第一次先有本地确认事务是否需要提交
     * @param t
     * @return
     */
    DelayMQTransactionState executeLocalTransaction(final T t,String msgId);

    /**
     * 回调确认是否需要提交
     * @param t
     * @return
     */
    DelayMQTransactionState checkLocalTransaction(final T t,String msgId);

    /**
     * 事务消息提交后的扩展（第一次本地确认 和回调确认都会触发）
     * @param t
     */
    void afterCommitTransactionMessage(final T t,String msgId);

    /**
     * 事务消息回滚的扩展（当因为超过重试次数造成的事务消息回滚 不会触发此事件）
     * @param t
     */
    void afterRollbackTransactionMessage(final T t,String msgId);

    /**
     * 超过次数回滚事务消息（超过重试次数触发）
     * @param t
     */
    void afterOverRetryTransactionMessage(final T t,String msgId);

    /**
     * 事务消息待确认的扩展（异常+不确定 触发）
     * @param t
     */
    void afterDelayTransactionMessage(final T t,String msgId);
}
