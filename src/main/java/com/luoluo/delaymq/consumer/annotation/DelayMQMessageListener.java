package com.luoluo.delaymq.consumer.annotation;

import com.luoluo.delaymq.constant.ConsumeMode;
import com.luoluo.delaymq.constant.QueueTypeEnum;

import java.lang.annotation.*;

/**
 * 消费者注解
 *
 * @Date: 2020/07/20
 * @Author: luoluo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DelayMQMessageListener {

    /**
     * 消费者组
     */
    String consumerGroup();

    /**
     * topic
     */
    String topic();

    /**
     * 默认并行消费
     */
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

    /**
     * 默认REIDIS
     */
    QueueTypeEnum queueType() default QueueTypeEnum.REDIS_QUEUE;

    /**
     * 服务启动回溯时间 -1代表
     */
    int reverseTime() default Integer.MIN_VALUE;

    /**
     * 消费队列数
     */
    int consumeThread() default Integer.MIN_VALUE;

    /**
     * 最大消费队列数.
     */
    int consumeThreadMax() default Integer.MIN_VALUE;

    /**
     * 消费超时
     */
    int consumeTimeout() default Integer.MIN_VALUE;

    /**
     * 重试次数
     */
    int retryCount() default Integer.MIN_VALUE;

    /**
     * 重试时间
     */
    int retryDelayTime() default Integer.MIN_VALUE;

    /**
     * 回溯时间
     */
    int backTrackTime() default Integer.MIN_VALUE;

    /**
     * 拉取数目
     */
    int pullMessageSize() default Integer.MIN_VALUE;

    /**
     * 是否支持事务
     */
    boolean supportTransaction() default false;
}
