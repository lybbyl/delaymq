package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.consumer.*;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.utils.ThreadFactoryImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2020/07/20
 * @descrn 统一监听管理容器
 */
@Slf4j
public class DefaultMergeDelayMQListenerContainer implements DelayMQListenerContainer, SmartLifecycle, Runnable {

    /**
     * 操作类型
     */
    protected MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    /**
     * 消费端配置
     */
    protected DelayMQProperties.Consumer consumerProperties;

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    /**
     * 启动标识
     */
    private boolean running;

    /**
     * topic对应监听缓存
     */
    protected Map<String, List<ConsumeListenerDTO>> topicConsumeListener = new ConcurrentHashMap<>();

    /**
     * 缓存任务队列
     */
    protected LinkedBlockingQueue<MQConsumer> mqConsumerRunnable = new LinkedBlockingQueue<>(1024);

    /**
     * 扫描线程
     */
    private final ScheduledExecutorService scanConsumerSchedule = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("scanConsumerSchedule", true));

    /**
     * topic管理容器
     */
    protected TopicManager topicManager = TopicManager.getInstance();

    /**
     * 执行线程数
     */
    @Setter
    @Getter
    protected int consumeThread;

    /**
     * 调度线程池
     */
    @Setter
    ThreadPoolExecutor consumeExecutor;

    public DefaultMergeDelayMQListenerContainer(DelayMQProperties.Consumer consumer, DistributedLock distributedLock) {
        this.consumerProperties = consumer;
        this.distributedLock = distributedLock;
    }

    /**
     * 注册topic管理消费者
     *
     * @param topicName
     * @param consumeListenerDTO
     */
    public void registerTopicConsume(String topicName, ConsumeListenerDTO consumeListenerDTO) {
        initDelayMQPushConsumer(consumeListenerDTO);
        List<ConsumeListenerDTO> topicConsumeList = topicConsumeListener.get(topicName);
        if (topicConsumeList == null) {
            topicConsumeListener.putIfAbsent(topicName, new ArrayList<>());
        } else {
            for (ConsumeListenerDTO dto : topicConsumeList) {
                if (consumeListenerDTO.getConsumeGroup().equals(dto.getConsumeGroup()) && consumeListenerDTO.getAnnotation().queueType().equals(dto.getAnnotation().queueType())) {
                    log.error("please check topic:{} and consumeGroup:{} and queueType:{} already exist", dto.getTopic(), dto.getConsumeGroup(), dto.getAnnotation().queueType());
                    throw new BizException("please check topic:" + dto.getTopic() + " and consumeGroup:" + dto.getConsumeGroup() + " and queueType:" + dto.getAnnotation().queueType() + " already exist");
                }
            }
        }
        topicConsumeListener.get(topicName).add(consumeListenerDTO);
    }

    /**
     * 初始化DelayMQPushConsumer()
     */
    protected void initDelayMQPushConsumer(ConsumeListenerDTO consumeListenerDTO) {
        if (consumeListenerDTO.getDelayMQConsumerListener() == null) {
            throw new IllegalArgumentException("no such delayMQConsumerListener");
        }
        Assert.notNull(consumeListenerDTO.getConsumeGroup(), "Property 'consumer group ' is required");
        Assert.notNull(consumeListenerDTO.getTopic(), "Property 'topic' is required");
        Assert.notNull(messageOperateManager.getMessageOperate(consumeListenerDTO.getAnnotation().queueType()), "no match queueType");
        MQConsumer consumer = new DefaultMQConsumer(consumeListenerDTO.getConsumeGroup(), consumeListenerDTO.getTopic(), topicManager, consumeListenerDTO.getAnnotation(), distributedLock, consumerProperties);
        consumer.initialize(consumeListenerDTO.getDelayMQConsumerListener());
        // 预处理接口 提供扩展方法
        if (consumeListenerDTO.getDelayMQConsumerListener() instanceof DelayMQConsumerLifecycleListener) {
            ((DelayMQConsumerLifecycleListener) consumeListenerDTO.getDelayMQConsumerListener()).prepareStart(consumer);
        }
        consumeListenerDTO.setConsumer(consumer);
    }

    /**
     * 扫描调度 任务如queue
     */
    @Override
    public void run() {
        for (Map.Entry<String, List<ConsumeListenerDTO>> entry : topicConsumeListener.entrySet()) {
            String topicName = entry.getKey();
            List<ConsumeListenerDTO> consumeListenerDTOS = entry.getValue();
            for (ConsumeListenerDTO consumeListenerDTO : consumeListenerDTOS) {
                TopicQueue topicQueue = topicManager.getTopicQueue(topicName, consumeListenerDTO.getAnnotation().queueType(), false);
                if (topicQueue != null) {
                    int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
                    for (int i = 0; i < queueSize; i++) {
                        mqConsumerRunnable.offer(consumeListenerDTO.getConsumer());
                    }
                }
            }
        }
    }

    /**
     * 执行任务
     */
    private class ConsumeRunnable implements Runnable {
        @Override
        public void run() {
            while (isRunning()) {
                try {
                    MQConsumer mqConsumer = mqConsumerRunnable.take();
                    mqConsumer.run();
                } catch (Exception e) {
                    log.error("ConsumeRunnable error", e);
                }
            }
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
        this.setRunning(true);
        scanConsumerSchedule.scheduleAtFixedRate(this, 1000 * 3, 1000, TimeUnit.MILLISECONDS);
        ConsumeRunnable consumeRunnable = new ConsumeRunnable();
        for (int i = 0; i < consumeThread; i++) {
            consumeExecutor.execute(consumeRunnable);
        }
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
        // 最先关闭 最后启动
        return Integer.MAX_VALUE;
    }
}
