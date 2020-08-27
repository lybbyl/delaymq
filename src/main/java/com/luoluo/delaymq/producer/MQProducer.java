package com.luoluo.delaymq.producer;

import com.luoluo.delaymq.Lifecycle;
import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.common.SendResultCallback;
import com.luoluo.delaymq.common.SendResultFuture;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.concurrent.Future;

/**
 * 生产者
 *
 * @Date: 2020/7/8 14:56
 * @Author: luoluo
 * @Description:
 */
public interface MQProducer extends Lifecycle, SmartInitializingSingleton {

    /**
     * 同步发送消息
     *
     * @param t         消息体  消息体
     * @param topicName topic topic
     * @param <T>
     * @return msgId
     */
    <T> String sendMessage(T t, String topicName);

    /**
     * 同步发送
     *
     * @param t         消息体 消息体
     * @param topicName topic topic
     * @param timestamp 执行时间戳 执行时间戳
     * @param <T>
     * @return msgId
     */
    <T> String sendMessage(T t, String topicName, Long timestamp);

    /**
     * 同步发送
     *
     * @param t         消息体 消息体
     * @param topicName topic topic
     * @param <T>
     * @return msgId
     */
    <T> String sendMessage(T t, String topicName, String msgId);

    /**
     * 同步发送
     *
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param msgId     消息id 应保证全局唯一 如果不传 则自动使用uuid
     * @param <T>
     * @return msgId
     */
    <T> String sendMessage(T t, String topicName, Long timestamp, String msgId);

    /**
     * 同步发送
     *
     * @param message
     * @return
     */
    String sendMessage(Message<?> message);

    /***
     * hashkey 发送
     * @param t 消息体
     * @param topicName topic
     * @param hashKey
     * @param <T>
     * @return
     */
    <T> String hashSendMessage(T t, String topicName, String hashKey);

    /**
     * hashkey 发送
     *
     * @param t         消息体
     * @param topicName topic
     * @param hashKey
     * @param timestamp 执行时间戳 带时间戳
     * @param <T>
     * @return
     */
    <T> String hashSendMessage(T t, String topicName, String hashKey, Long timestamp);

    /**
     * 消息id
     *
     * @param t         消息体
     * @param topicName topic
     * @param hashKey
     * @param <T>
     * @return
     */
    <T> String hashSendMessage(T t, String topicName, String hashKey, String msgId);

    /**
     * hash发送
     *
     * @param t         消息体
     * @param topicName topic
     * @param hashKey
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> String hashSendMessage(T t, String topicName, String hashKey, Long timestamp, String msgId);

    /**
     * hashkey 发送
     *
     * @param message
     * @param hashKey
     * @return
     */
    String hashSendMessage(Message<?> message, String hashKey);

    /**
     * 同步发送 包裹异常
     *
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp);

    /**
     * 同步发送 包裹异常
     *
     * @param t         消息体
     * @param topicName topic
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, String msgId);

    /**
     * 同步发送 包裹异常
     *
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, String msgId);

    /**
     * 同步发送 设置超时
     *
     * @param t         消息体
     * @param topicName topic
     * @param timeout
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, Integer timeout);

    /**
     * 同步发送 设置超时
     *
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param timeout
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, Integer timeout);

    /**
     * 同步发送 设置超时
     *
     * @param t         消息体
     * @param topicName topic
     * @param timeout
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, String msgId, Integer timeout);

    /**
     * 同步发送 设置超时
     *
     * @param t         消息体
     * @param topicName topic
     * @param timeout
     * @param <T>
     * @return
     */
    <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, String msgId, Integer timeout);

    /**
     * 无超时
     *
     * @param message
     * @return
     */
    SendResultFuture syncSendMessage(Message<?> message);

    /**
     * @param message
     * @param timeout
     * @return
     */
    SendResultFuture syncSendMessage(Message<?> message, Integer timeout);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param <T>
     * @return
     */
    <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, Long timestamp);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param <T>
     * @return
     */
    <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, String msgId);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, Long timestamp, String msgId);

    /**
     * @param message
     * @return
     */
    Future<SendResultFuture> asyncSendMessageFuture(Message<?> message);

    /**
     * @param t                  消息体
     * @param topicName          topic
     * @param sendResultCallback
     * @param <T>
     */
    <T> void asyncSendMessageCallback(T t, String topicName, SendResultCallback sendResultCallback);

    /**
     * @param t                  消息体
     * @param topicName          topic
     * @param timestamp          执行时间戳
     * @param sendResultCallback
     * @param <T>
     */
    <T> void asyncSendMessageCallback(T t, String topicName, Long timestamp, SendResultCallback sendResultCallback);

    /**
     * @param t                  消息体
     * @param topicName          topic
     * @param sendResultCallback
     * @param <T>
     * @return msgId
     */
    <T> void asyncSendMessageCallback(T t, String topicName, String msgId, SendResultCallback sendResultCallback);

    /**
     * @param t                  消息体
     * @param topicName          topic
     * @param timestamp          执行时间戳
     * @param sendResultCallback
     * @param <T>
     * @return msgId
     */
    <T> void asyncSendMessageCallback(T t, String topicName, Long timestamp, String msgId, SendResultCallback sendResultCallback);

    /**
     * @param message
     * @param sendResultCallback
     * @param <T>
     */
    <T> void asyncSendMessageCallback(Message<T> message, SendResultCallback sendResultCallback);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> String sendTransactionMessage(T t, String topicName, Long timestamp);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param <T>
     * @return
     */
    <T> String sendTransactionMessage(T t, String topicName, String msgId);

    /**
     * @param t         消息体
     * @param topicName topic
     * @param timestamp 执行时间戳
     * @param <T>
     * @return
     */
    <T> String sendTransactionMessage(T t, String topicName, Long timestamp, String msgId);

    /**
     * @param message
     * @return
     */
    String sendTransactionMessage(Message<?> message);
}
