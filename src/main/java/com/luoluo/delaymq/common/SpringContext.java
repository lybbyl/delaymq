package com.luoluo.delaymq.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 记录context
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public class SpringContext implements ApplicationContextAware {

    static ApplicationContext context;

    /**
     * 此处比afterPropertiesSet() 更先执行
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getAppContext() {
        return context;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static <T> T getBean(String name) throws BeansException {
        return (T) context.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return (T) context.getBean(requiredType);
    }
}
