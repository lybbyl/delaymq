package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.producer.DelayMQLocalTransactionListener;
import com.luoluo.delaymq.producer.TransactionListenerContainer;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName DelayMQAutoConfiguration
 * @descrn 事务监听bean Processor 解析@DelayMQTransactionListener注解
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@Slf4j
public class DelayMQTransactionAnnotationProcessor implements BeanPostProcessor, Ordered {

    private final Set<Class<?>> nonProcessedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>(64));
    private TransactionListenerContainer transactionListenerContainer = TransactionListenerContainer.getInstance();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 当bean被初始化后 后置处理
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonProcessedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            DelayMQTransactionListener anno = AnnotationUtils.findAnnotation(targetClass, DelayMQTransactionListener.class);
            this.nonProcessedClasses.add(bean.getClass());
            if (anno == null) {
                log.trace("No @DelayMQTransactionListener annotations found on bean type: {}", bean.getClass());
            } else {
                try {
                    processTransactionListenerAnnotation(bean);
                } catch (BizException e) {
                    log.error("Failed to process annotation " + anno, e);
                    throw new BeanCreationException("Failed to process annotation " + anno, e);
                }
            }
        }
        return bean;
    }

    private void processTransactionListenerAnnotation(Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
        if (!DelayMQLocalTransactionListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + DelayMQLocalTransactionListener.class.getName());
        }
        DelayMQTransactionListener annotation = clazz.getAnnotation(DelayMQTransactionListener.class);
        transactionListenerContainer.registerTransactionHandler(annotation, (DelayMQLocalTransactionListener) bean);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
