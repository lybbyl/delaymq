package com.luoluo.delaymq.producer.annotation;

import com.luoluo.delaymq.constant.QueueTypeEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务消息注解
 * @Date: 2020/07/20
 * @Author: luoluo
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DelayMQTransactionListener {

    QueueTypeEnum queueType() default QueueTypeEnum.REDIS_QUEUE;

    String topicName();
}
