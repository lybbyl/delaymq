package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.ConsumeGroupType;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.consumer.ConsumeListenerDTO;
import com.luoluo.delaymq.consumer.DelayMQConsumerListener;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.utils.ThreadFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2020/07/20
 * @descrn 容器管理 初始化监听bean
 */
@Slf4j
public class DefaultListenerContainerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    /**
     * 全局上下文
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     *
     */
    private Map<QueueTypeEnum, Map<String, List<String>>> topicConsumerGroupRecord = new ConcurrentHashMap<>(64);

    /**
     * bean++
     */
    private AtomicLong counter = new AtomicLong(0);

    /**
     * environment
     */
    protected StandardEnvironment environment;

    /**
     * 配置
     */
    protected DelayMQProperties delayMQProperties;

    /**
     * topicManager
     */
    protected TopicManager topicManager = TopicManager.getInstance();

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    /**
     * 合并管理容器
     */
    protected DefaultMergeDelayMQListenerContainer defaultMergeDelayMQListenerContainer;

    /**
     * 操作queue管理
     */
    private MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    /**
     * 扫描topic
     */
    private final ScheduledExecutorService scanTopicManagerSchedule = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "scanTopicManagerSchedule", true));

    /**
     * 通过构造器注入
     *
     * @param environment       环境
     * @param delayMQProperties 配置文件
     */
    public DefaultListenerContainerConfiguration(StandardEnvironment environment, DelayMQProperties delayMQProperties, DistributedLock distributedLock) {
        this.environment = environment;
        this.delayMQProperties = delayMQProperties;
        this.distributedLock = distributedLock;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * 当beanFactory完成所有的bean实例化之后 则会寻找所有继承SmartInitializingSingleton 调用afterSingletonsInstantiated 类似一个扩展机制
     * 1：获取所有DelayMQMessageListener 监听bean
     * 2：调用registerContainer 注册自己 成为观察者 以便后续回调
     */
    @Override
    public void afterSingletonsInstantiated() {
        //获取所有含有@DelayMQMessageListener 的bean
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(DelayMQMessageListener.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //查看是统一管理 还是分开管理
        if (ConsumeGroupType.MERGE.equals(delayMQProperties.getConsumer().getConsumeGroupType())) {
            createMergeDelayMQListenerContainer(beans);
        } else if (ConsumeGroupType.PART.equals(delayMQProperties.getConsumer().getConsumeGroupType())) {
            beans.forEach(this::registerPartContainer);
        }
        scanTopicManagerSchedule.scheduleAtFixedRate(topicManager, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 统一管理
     * 创建管理器 依次注册bean
     * 启动容器
     *
     * @param beans
     */
    private void createMergeDelayMQListenerContainer(Map<String, Object> beans) {
        //获取容器名称 作为未来的监听容器
        String containerBeanName = DefaultMergeDelayMQListenerContainer.class.getName();
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        //注册一个容器bean -->regeist beanDefination
        genericApplicationContext.registerBean(containerBeanName, DefaultMergeDelayMQListenerContainer.class,
                () -> createDelayMQMergeListenerContainer());

        //获取一个容器bean 此过程涉及到bean的实例化
        DefaultMergeDelayMQListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                DefaultMergeDelayMQListenerContainer.class);

        beans.forEach(this::registerMergeContainer);

        //启动容器
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 统一管理容器bean实例化
     *
     * @return
     */
    protected DefaultMergeDelayMQListenerContainer createDelayMQMergeListenerContainer() {
        this.defaultMergeDelayMQListenerContainer = new DefaultMergeDelayMQListenerContainer(delayMQProperties.getConsumer(), distributedLock);
        int mergeConsumeThread = delayMQProperties.getConsumer().getMergeConsumeThread();
        if (mergeConsumeThread <= 0) {
            mergeConsumeThread = 16;
        }
        defaultMergeDelayMQListenerContainer.setConsumeThread(mergeConsumeThread);
        ThreadPoolExecutor consumeExecutor = new ThreadPoolExecutor(
                mergeConsumeThread,
                mergeConsumeThread,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(mergeConsumeThread),
                new ThreadFactoryImpl("MergePullMessageTask"),
                new ThreadPoolExecutor.DiscardPolicy());
        defaultMergeDelayMQListenerContainer.setConsumeExecutor(consumeExecutor);
        return defaultMergeDelayMQListenerContainer;
    }

    /**
     * 注册到统一管理器当中
     *
     * @param beanName
     * @param bean
     */
    private void registerMergeContainer(String beanName, Object bean) {
        DelayMQMessageListener annotation = getDelayMQMessageListener(bean);
        if (annotation == null) {
            return;
        }
        Assert.notNull(messageOperateManager.getMessageOperate(annotation.queueType()), String.format("No matching queue operation type found:%s, please check %s", annotation.queueType(), beanName));

        ConsumeListenerDTO dto = new ConsumeListenerDTO();
        dto.setAnnotation(annotation);
        dto.setConsumeGroup(annotation.consumerGroup());
        dto.setDelayMQConsumerListener((DelayMQConsumerListener) bean);
        dto.setTopic(annotation.topic());
        this.defaultMergeDelayMQListenerContainer.registerTopicConsume(annotation.topic(), dto);
    }

    /**
     * 分开管理  每个监听bean都初始化一个容器
     * 即每个bean由自我调度
     *
     * @param beanName
     * @param bean
     */
    private void registerPartContainer(String beanName, Object bean) {
        DelayMQMessageListener annotation = getDelayMQMessageListener(bean);
        if (annotation == null) {
            return;
        }
        topicConsumerGroupRecord.putIfAbsent(annotation.queueType(), new ConcurrentHashMap<>());
        Map<String, List<String>> topicConsumerGroupMap = topicConsumerGroupRecord.get(annotation.queueType());
        List<String> consumerGroupList = topicConsumerGroupMap.get(annotation.topic());
        if (consumerGroupList == null) {
            consumerGroupList = new ArrayList<>();
            topicConsumerGroupMap.put(annotation.topic(), consumerGroupList);
        } else {
            for (String consumerGroup : consumerGroupList) {
                if (consumerGroup.equals(annotation.consumerGroup())) {
                    log.error("please check topic:{} and consumeGroup:{} and queueType:{} already exist", annotation.topic(), annotation.consumerGroup(), annotation.queueType());
                    throw new BizException("please check topic:" + annotation.topic() + " and consumeGroup:" + annotation.consumerGroup() + "and queueType:" + annotation.queueType() + " already exist");
                }
            }
        }
        topicConsumerGroupMap.get(annotation.topic()).add(annotation.consumerGroup());
        //获取容器名称 作为未来的监听容器
        String containerBeanName = String.format("%s_%s", DefaultPartDelayMQListenerContainer.class.getName(),
                counter.incrementAndGet());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        //注册一个容器bean -->regeist beanDefination
        genericApplicationContext.registerBean(containerBeanName, DefaultPartDelayMQListenerContainer.class,
                () -> createDelayMQPartListenerContainer(containerBeanName, bean, annotation, delayMQProperties.getConsumer()));
        //获取一个容器bean 此过程涉及到bean的实例化
        DefaultPartDelayMQListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                DefaultPartDelayMQListenerContainer.class);

        //启动容器
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }

        log.info("Register the listener to container, listenerBeanName:{}, containerBeanName:{}", beanName, containerBeanName);
    }

    /**
     * 初始化DefaultDelayMQListenerContainer(监听bean各自管理容器)
     *
     * @param name
     * @param bean
     * @param annotation
     * @param consumer
     * @return
     */
    protected DefaultPartDelayMQListenerContainer createDelayMQPartListenerContainer(String name, Object bean,
                                                                                     DelayMQMessageListener annotation, DelayMQProperties.Consumer consumer) {
        DefaultPartDelayMQListenerContainer container = new DefaultPartDelayMQListenerContainer(distributedLock);

        container.setName(name);
        //设置监听的topic
        container.setTopic(environment.resolvePlaceholders(annotation.topic()));
        container.setConsumerGroup(environment.resolvePlaceholders(annotation.consumerGroup()));
        container.setAnno(annotation);
        container.setDelayMQConsumerListener((DelayMQConsumerListener) bean);
        container.setConsumerProperties(consumer);

        Assert.notNull(messageOperateManager.getMessageOperate(annotation.queueType()), "No matching queue operation type found");
        TopicQueue topicQueue = topicManager.getTopicQueue(container.getTopic(), annotation.queueType(), false);
        int consumeThread;
        int consumeThreadMax;
        if (null != topicQueue) {
            consumeThread = topicQueue.getTopicQueueData().getQueueNames().size();
            consumeThreadMax = 2 * consumeThread;
        } else {
            consumeThread = 1;
            consumeThreadMax = 1;
        }
        if (annotation.consumeThread() > MQConstant.CONSUME_ZERO_SIZE) {
            consumeThread = annotation.consumeThread();
            consumeThreadMax = 2 * consumeThread;
        }
        if (annotation.consumeThreadMax() > consumeThreadMax) {
            consumeThreadMax = annotation.consumeThreadMax();
        }
        container.setConsumeThread(consumeThread);
        container.setConsumeThreadMax(consumeThreadMax);

        ThreadPoolExecutor consumeExecutor = new ThreadPoolExecutor(
                consumeThread,
                consumeThreadMax,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(consumeThread),
                new ThreadFactoryImpl("PartPullMessageTask"),
                new ThreadPoolExecutor.DiscardPolicy());
        container.setConsumeExecutor(consumeExecutor);
        return container;
    }

    /**
     * 解析注解
     *
     * @param bean
     * @return
     */
    private DelayMQMessageListener getDelayMQMessageListener(Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
        if (!DelayMQConsumerListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + DelayMQConsumerListener.class.getName());
        }
        DelayMQMessageListener annotation = clazz.getAnnotation(DelayMQMessageListener.class);
        String consumerGroup = this.environment.resolvePlaceholders(annotation.consumerGroup());
        String topic = this.environment.resolvePlaceholders(annotation.topic());
        //注册topic
        topicManager.registerTopicQueue(topic, annotation.queueType());
        boolean listenerEnabled = (boolean) delayMQProperties.getConsumer().getListeners().getOrDefault(consumerGroup, Collections.EMPTY_MAP).getOrDefault(topic, true);
        if (!listenerEnabled) {
            log.debug("Consumer Listener (group:{},topic:{}) is not enabled by configuration, will ignore initialization.", consumerGroup, topic);
            return null;
        }
        validate(annotation);
        return annotation;
    }

    /**
     * todo 待扩展
     */
    private void validate(DelayMQMessageListener annotation) {

    }
}
