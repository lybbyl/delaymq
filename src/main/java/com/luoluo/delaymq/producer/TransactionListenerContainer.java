package com.luoluo.delaymq.producer;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.producer.annotation.AbstractDelayMQTransactionListener;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事务监听bean容器
 *
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public class TransactionListenerContainer implements DisposableBean {

    /**
     * 单例容器
     */
    private TransactionListenerContainer() {
    }

    private static TransactionListenerContainer instance = new TransactionListenerContainer();

    public static TransactionListenerContainer getInstance() {
        return instance;
    }

    /**
     * 缓存监听bean
     */
    private final Map<QueueTypeEnum, Map<String, TransactionListenerPhrase>> listenerContainers = new ConcurrentHashMap<>(4);

    @Override
    public void destroy() {
        listenerContainers.clear();
    }

    public int getListenerSize(QueueTypeEnum queueType) {
        Map<String, TransactionListenerPhrase> queueListenerMap = listenerContainers.get(queueType);
        if (queueListenerMap != null) {
            return queueListenerMap.keySet().size();
        }
        return 0;
    }

    /**
     * 注册事务监听bean
     * 一个事务消息发送类 必须有且一个该topic事务监听的bean
     *
     * @param anno
     * @param delayMQLocalTransactionListener
     * @throws BizException
     */
    public void registerTransactionHandler(DelayMQTransactionListener anno, DelayMQLocalTransactionListener delayMQLocalTransactionListener)
            throws BizException {
        listenerContainers.putIfAbsent(anno.queueType(), new ConcurrentHashMap<>(32));
        Map<String, TransactionListenerPhrase> queueListenerMap = listenerContainers.get(anno.queueType());
        if (queueListenerMap.containsKey(anno.topicName())) {
            throw new BizException(String.format(
                    "topicName [%s] has been defined in TransactionListener [%s]",
                    anno.topicName(), delayMQLocalTransactionListener.getClass()), null);
        }
        TransactionListenerPhrase phrase = new TransactionListenerPhrase();
        phrase.setDelayMQLocalTransactionListener(delayMQLocalTransactionListener);
        phrase.setMessageType(getMessageType(delayMQLocalTransactionListener));
        queueListenerMap.putIfAbsent(anno.topicName(), phrase);
    }

    private Type getMessageType(DelayMQLocalTransactionListener delayMQLocalTransactionListener) {
        Class<?> targetClass;
        targetClass = AopProxyUtils.ultimateTargetClass(delayMQLocalTransactionListener);
        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            Type type = targetClass.getGenericSuperclass();
            if (Objects.nonNull(type)) {
                if (type instanceof ParameterizedType &&
                        Objects.equals(((ParameterizedType) type).getRawType(), AbstractDelayMQTransactionListener.class)) {
                    matchedGenericInterface = type;
                    break;
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        if (!Objects.isNull(matchedGenericInterface)) {
            Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
            if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
                return actualTypeArguments[0];
            }
        }
        targetClass = AopProxyUtils.ultimateTargetClass(delayMQLocalTransactionListener);
        Type[] interfaces = targetClass.getGenericInterfaces();
        Class<?> superclass = targetClass.getSuperclass();
        while ((Objects.isNull(interfaces) || 0 == interfaces.length) && Objects.nonNull(superclass)) {
            interfaces = superclass.getGenericInterfaces();
            superclass = targetClass.getSuperclass();
        }
        if (Objects.nonNull(interfaces)) {
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    if (Objects.equals(parameterizedType.getRawType(), DelayMQLocalTransactionListener.class)) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
                            return (Class) actualTypeArguments[0];
                        } else {
                            return Object.class;
                        }
                    }
                }
            }
            return Object.class;
        } else {
            return Object.class;
        }
    }

    public TransactionListenerPhrase getDelayMQLocalTransactionListener(Message<?> message, QueueTypeEnum queueType) {
        return listenerContainers.getOrDefault(queueType, new ConcurrentHashMap<>()).get(message.getTopicName());
    }
}
