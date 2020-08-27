package com.luoluo.delaymq.consumer;

import com.luoluo.delaymq.common.SpringContext;
import com.luoluo.delaymq.common.TopicChangeListener;
import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.ConsumeMode;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.mysql.RecordConsumeTime;
import com.luoluo.delaymq.utils.DateNumUtil;
import lombok.NoArgsConstructor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName DefaultMQConsumer
 * @Description: 消费调度默认实现
 * @Author luoluo
 * @Date 2020/7/10
 * @Version V1.0
 **/
@NoArgsConstructor
public class DefaultMQConsumer implements MQConsumer, TopicChangeListener {

    /**
     * 分布式锁
     */
    protected DistributedLock distributedLock;

    /**
     * topic管理器
     */
    protected TopicManager topicManager;

    /**
     * topic
     */
    protected String topic;

    /**
     * 消费者组
     */
    protected String consumerGroup;

    /**
     * 消费端配置
     */
    protected DelayMQProperties.Consumer consumerProperties;

    /**
     * 消费注解 记录下配置值
     */
    protected DelayMQMessageListener anno;
    protected int retryDelayTime;
    protected int retryCount;

    /**
     * 核心消费服务
     */
    protected CommonConsumeMessageServiceImpl commonConsumeMessageServiceImpl;

    /**
     * 记录队列 消费到的最新位点
     */
    private ConcurrentHashMap<Integer, Long> queueBeginTimeStamp;

    /**
     * 顺序消费自增
     */
    private AtomicLong incr = new AtomicLong(0L);

    /**
     * 后续扩展 用来
     */
    private AtomicLong counter = new AtomicLong(0);

    public DefaultMQConsumer(String consumerGroup, String topic, TopicManager topicManager, DelayMQMessageListener delayMQMessageListener, DistributedLock distributedLock, DelayMQProperties.Consumer consumer) {
        this.consumerGroup = consumerGroup;
        this.topic = topic;
        this.topicManager = topicManager;
        this.anno = delayMQMessageListener;
        this.distributedLock = distributedLock;
        this.consumerProperties = consumer;
    }

    /**
     * 实时获取topic信息 初始化起始消费值
     * 初始化消费服务到spring容器
     * 注册topic变更监听
     *
     * @param delayMQConsumerListener
     */
    @Override
    public void initialize(DelayMQConsumerListener delayMQConsumerListener) {
        Assert.notNull(anno, "Property 'anno' is required");

        this.retryCount = anno.retryCount();
        if (retryCount == Integer.MIN_VALUE) {
            retryCount = consumerProperties.getRetryCount();
        }
        if (retryCount <= MQConstant.CONSUME_ZERO_SIZE) {
            retryCount = MQConstant.DEFAULT_CONSUME_RETRY_OUT;
        }

        this.retryDelayTime = anno.retryDelayTime();
        if (retryDelayTime == Integer.MIN_VALUE) {
            retryDelayTime = consumerProperties.getRetryDelayTime();
        }
        if (retryDelayTime <= MQConstant.CONSUME_ZERO_SIZE) {
            retryDelayTime = MQConstant.DEFAULT_CONSUME_RETRY_DELAY_TIME;
        }

        //获取容器名称 作为未来的监听容器
        String containerBeanName = String.format("%s_%s", CommonConsumeMessageServiceImpl.class.getName(),
                AopProxyUtils.ultimateTargetClass(delayMQConsumerListener).getName());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) SpringContext.getAppContext();
        //注册一个容器bean -->regeist beanDefination
        genericApplicationContext.registerBean(containerBeanName, CommonConsumeMessageServiceImpl.class, () -> getConsumerService(delayMQConsumerListener));
        //获取一个容器bean 注册消费核心服务
        this.commonConsumeMessageServiceImpl = genericApplicationContext.getBean(containerBeanName,
                CommonConsumeMessageServiceImpl.class);
        this.commonConsumeMessageServiceImpl.setRetryCount(retryCount);
        this.commonConsumeMessageServiceImpl.setRetryDelayTime(retryDelayTime);
        //初始化 判断是否有事务注解
        this.commonConsumeMessageServiceImpl.init(containerBeanName);

        queueBeginTimeStamp = new ConcurrentHashMap<>(4);

        TopicQueue topicQueue = topicManager.getTopicQueue(topic, anno.queueType(),false);
        if (topicQueue != null) {
            putQueueBeginTime(topicQueue);
        }

        topicManager.registerTopicListener(topic, this);
    }

    /**
     * 初始化起始消费值
     *
     * @param topicQueue
     */
    private void putQueueBeginTime(TopicQueue topicQueue) {
        long defaultBeginTime;
        int reverseTime = consumerProperties.getReverseTime();
        if (anno.reverseTime() != Integer.MIN_VALUE) {
            reverseTime = anno.reverseTime();
        }
        if (reverseTime == -2) {
            defaultBeginTime = 0;
        } else if (reverseTime == -1) {
            //取记录集群东西
            List<RecordConsumeTime> beginTimes = commonConsumeMessageServiceImpl.getRecordTopicConsumeTime(consumerGroup, topic);
            int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
            for (int i = 0; i < queueSize; i++) {
                queueBeginTimeStamp.put(i, 0L);
            }
            if (beginTimes != null && beginTimes.size() > 0) {
                for (RecordConsumeTime recordConsumeTime : beginTimes) {
                    queueBeginTimeStamp.put(recordConsumeTime.getQueueNum(), recordConsumeTime.getConsumeTime());
                }
            }
            return;
        } else if (reverseTime > 0) {
            defaultBeginTime = System.currentTimeMillis() - reverseTime * DateNumUtil.SECOND;
        } else {
            defaultBeginTime = System.currentTimeMillis();
        }
        int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
        for (int i = 0; i < queueSize; i++) {
            queueBeginTimeStamp.putIfAbsent(i, defaultBeginTime);
        }
    }

    /**
     * topic变更事件
     *
     * @param topicQueue
     */
    @Override
    public void onTopicQueueChange(TopicQueue topicQueue) {

    }

    /**
     * 初始化监听
     *
     * @param delayMQConsumerListener
     */
    protected CommonConsumeMessageServiceImpl getConsumerService(DelayMQConsumerListener delayMQConsumerListener) {
        CommonConsumeMessageServiceImpl consumeMessageService = new CommonConsumeMessageServiceImpl(consumerGroup, topic, anno, consumerProperties, distributedLock, delayMQConsumerListener);
        consumeMessageService.setMessageOperate(SpringContext.getBean(anno.queueType().getMessageOperateClass()));
        return consumeMessageService;
    }

    /**
     * 如果是顺序消费 则通过CAS进行控制
     * 原始队列正常消费
     * 失败重试消费
     * 回溯消费
     */
    @Override
    public void run() {
        TopicQueue topicQueue = topicManager.getTopicQueue(topic, anno.queueType(),false);
        if (topicQueue != null) {
            int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
            long andIncrement = incr.getAndIncrement();
            int queueNum = (int) (andIncrement % queueSize);
            String lockKey = anno.consumerGroup() + queueNum;
            if (ConsumeMode.ORDERLY.equals(anno.consumeMode())) {
                if (!distributedLock.tryLock(lockKey)) {
                    return;
                }
            }
            try {
                long oldBeginTimeStamp = queueBeginTimeStamp.getOrDefault(queueNum, 0L);
                long newBeginTimeStamp = System.currentTimeMillis();
                commonConsumeMessageServiceImpl.consumeTopicMessage(topic, queueNum, oldBeginTimeStamp, newBeginTimeStamp, 0, false);
                commonConsumeMessageServiceImpl.consumeRetryTopicMessage(topic + MQConstant.MSG_DELAY, queueNum, oldBeginTimeStamp - retryDelayTime * retryCount * DateNumUtil.SECOND, newBeginTimeStamp, 0);
                commonConsumeMessageServiceImpl.backTrackTopicMessage(topic, queueNum, oldBeginTimeStamp, newBeginTimeStamp, 0);
                commonConsumeMessageServiceImpl.recordTopicConsumeTime(queueNum, newBeginTimeStamp);
                putBeginTimeStamp(queueNum, newBeginTimeStamp + 1);
            } finally {
                if (ConsumeMode.ORDERLY.equals(anno.consumeMode())) {
                    distributedLock.unlock(lockKey);
                }
            }
        }
    }

    /**
     * 时间片更新
     *
     * @param queueNum
     * @param endTimeStamp
     */
    private synchronized void putBeginTimeStamp(int queueNum, long endTimeStamp) {
        if (endTimeStamp > queueBeginTimeStamp.getOrDefault(queueNum, 0L)) {
            queueBeginTimeStamp.put(queueNum, endTimeStamp);
        }
    }

}
