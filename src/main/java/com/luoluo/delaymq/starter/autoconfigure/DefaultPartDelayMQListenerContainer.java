package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.consumer.*;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.utils.ThreadFactoryImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2020/07/20
 * @descrn 监听bean初始容器
 */
@Slf4j
public class DefaultPartDelayMQListenerContainer implements InitializingBean, DelayMQListenerContainer, SmartLifecycle, Runnable {

    @Setter
    @Getter
    protected String name;

    /**
     * 消费者组
     */
    @Setter
    @Getter
    protected String consumerGroup;

    @Setter
    @Getter
    protected String topic;

    @Setter
    @Getter
    protected TopicManager topicManager = TopicManager.getInstance();

    @Setter
    @Getter
    protected DelayMQMessageListener anno;

    @Setter
    @Getter
    protected DelayMQConsumerListener delayMQConsumerListener;

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    @Setter
    @Getter
    protected DelayMQProperties.Consumer consumerProperties;

    @Setter
    @Getter
    protected int consumeThread;

    @Setter
    @Getter
    protected int consumeThreadMax;

    /**
     * 消费者
     */
    private MQConsumer consumer;

    /**
     * 一个consumerGroup 对应一个扫描线程 n个调度线程
     */
    private final ScheduledExecutorService scanConsumerSchedule = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "scanConsumerSchedule", true));

    @Setter
    ThreadPoolExecutor consumeExecutor;

    private boolean running;

    public DefaultPartDelayMQListenerContainer(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    /**
     * 在aware之后激活类定义的init方法 ->afterPropertiesSet()
     * 初始化DelayMQPushConsumer()
     */
    @Override
    public void afterPropertiesSet() {
        initDelayMQPushConsumer();
    }

    /**
     * 初始化DelayMQPushConsumer()
     */
    protected MQConsumer initDelayMQPushConsumer() {
        if (delayMQConsumerListener == null) {
            throw new IllegalArgumentException("Property 'delayMQListener' is required");
        }
        Assert.notNull(consumerGroup, "Property 'consumer group ' is required");
        Assert.notNull(topic, "Property 'topic' is required");

        consumer = new DefaultMQConsumer(consumerGroup, topic, topicManager, anno, distributedLock, consumerProperties);
        consumer.initialize(delayMQConsumerListener);
        //预处理接口 扩展
        if (delayMQConsumerListener instanceof DelayMQConsumerLifecycleListener) {
            ((DelayMQConsumerLifecycleListener) delayMQConsumerListener).prepareStart(consumer);
        }
        return consumer;
    }

    @Override
    public void run() {
        for (int i = 0; i < consumeThread; i++) {
            consumeExecutor.execute(consumer);
        }
    }

    @Override
    public void destroy() {
        this.setRunning(false);
        scanConsumerSchedule.shutdown();
        consumeExecutor.shutdown();
        log.info("container destroyed, {}", this.toString());
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("container already running. " + this.toString());
        }
        scanConsumerSchedule.scheduleAtFixedRate(this, 1000 * 3, 1000, TimeUnit.MILLISECONDS);
        this.setRunning(true);

        log.info("running container: {}", this.toString());
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            scanConsumerSchedule.shutdown();
            consumeExecutor.shutdown();
            setRunning(false);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "DefaultDelayMQListenerContainer{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }

}
