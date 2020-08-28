package com.luoluo.delaymq.producer;

import com.luoluo.delaymq.common.*;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.ErrorCode;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.mysql.MySQLMessageOperate;
import com.luoluo.delaymq.redis.RedisMessageOperate;
import com.luoluo.delaymq.service.rebalance.Rebalance;
import com.luoluo.delaymq.service.rebalance.RebalanceStrategyEnum;
import com.luoluo.delaymq.service.rebalance.strategy.HashRebalanceImpl;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.delaymq.utils.ThreadFactoryImpl;
import com.luoluo.delaymq.utils.UtilAll;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @ClassName DefaultMQProducer
 * @Description: 默认发送实现
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class DefaultMQProducer implements MQProducer {

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    /**
     * 是否启动
     */
    private AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 事务管理
     */
    protected TopicManager topicManager = TopicManager.getInstance();

    /**
     * 配置
     */
    protected DelayMQProperties delayMQProperties;

    /**
     * 操作类型 对应REDIS延时队列 和MYSQL延时队列
     */
    protected MessageOperate messageOperate;

    /**
     * 事务消息的发送一定要确保含有事务监听bean 所以在生产者初始化事务消息消费者
     */
    private TransactionListenerContainer transactionListenerContainer = TransactionListenerContainer.getInstance();

    /**
     * 事务消息消费服务
     */
    private TransactionConsumeMessageService consumeMessageService;

    /**
     * 队列类型
     */
    private QueueTypeEnum queueType;

    /**
     * 异步发送线程池
     */
    ThreadPoolExecutor producerExecutor;

    public DefaultMQProducer(DelayMQProperties delayMQProperties, MessageOperate messageOperate, DistributedLock distributedLock) {
        this.messageOperate = messageOperate;
        this.delayMQProperties = delayMQProperties;
        this.distributedLock = distributedLock;
        if (messageOperate instanceof RedisMessageOperate) {
            this.queueType = QueueTypeEnum.REDIS_QUEUE;
            producerExecutor = new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors(),
                    1000 * 60,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(50000),
                    new ThreadFactoryImpl("redisPushTask"),
                    new ThreadPoolExecutor.AbortPolicy());
        } else if (messageOperate instanceof MySQLMessageOperate) {
            this.queueType = QueueTypeEnum.MYSQL_QUEUE;
            producerExecutor = new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors(),
                    1000 * 60,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(3000),
                    new ThreadFactoryImpl("mysqlPushTask"),
                    new ThreadPoolExecutor.AbortPolicy());
        } else {
            throw new BizException("No matching queue operation type found", null);
        }

    }

    /**
     * 当bean被初始化后启动
     */
    @Override
    public void afterSingletonsInstantiated() {
        this.start();
    }

    /**
     * 启动初始化
     */
    @Override
    public void start() {
        running.compareAndSet(false, true);
        if (transactionListenerContainer.getListenerSize(queueType) > 0) {
            consumeMessageService = new TransactionConsumeMessageService(distributedLock, messageOperate, queueType);
            consumeMessageService.start();
        }
    }

    /**
     * 优雅关闭
     */
    @Override
    public void stop() {
        if (!running.get()) {
            running.compareAndSet(true, false);
            consumeMessageService.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public <T> String sendMessage(T t, String topicName) {
        return this.sendMessage(t, topicName, System.currentTimeMillis());
    }

    @Override
    public <T> String sendMessage(T t, String topicName, Long timestamp) {
        Assert.notNull(timestamp, "'timestamp' must be not null");
        return this.sendMessage(t, topicName, timestamp, null);
    }

    @Override
    public <T> String sendMessage(T t, String topicName, String msgId) {
        return this.sendMessage(t, topicName, System.currentTimeMillis(), msgId);
    }

    @Override
    public <T> String sendMessage(T t, String topicName, Long timestamp, String msgId) {
        Assert.notNull(timestamp, "'timestamp' must be not null");
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(timestamp);
        message.setBody(t);
        message.setMsgId(msgId);
        return this.sendMessage(message);
    }

    @Override
    public String sendMessage(Message<?> message) {
        String queueName = validateAndGetQueueNum(message);
        return this.pushMessageToQueue(queueName, message);
    }

    @Override
    public <T> String hashSendMessage(T t, String topicName, String hashKey) {
        return this.hashSendMessage(t, topicName, hashKey, System.currentTimeMillis());
    }

    @Override
    public <T> String hashSendMessage(T t, String topicName, String hashKey, Long timestamp) {
        Assert.notNull(timestamp, "'timestamp' must be not null");
        return this.hashSendMessage(t, topicName, hashKey, timestamp, null);
    }

    @Override
    public <T> String hashSendMessage(T t, String topicName, String hashKey, String msgId) {
        return this.hashSendMessage(t, topicName, hashKey, System.currentTimeMillis(), msgId);
    }

    @Override
    public <T> String hashSendMessage(T t, String topicName, String hashKey, Long timestamp, String msgId) {
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(timestamp);
        message.setBody(t);
        message.setMsgId(msgId);
        return this.hashSendMessage(message, hashKey);
    }

    @Override
    public String hashSendMessage(Message<?> message, String hashKey) {
        String queueName = validateAndHashGetQueueNum(message, hashKey);
        return this.pushMessageToQueue(queueName, message);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp) {
        return this.syncSendMessage(t, topicName, timestamp, null, null);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, String msgId) {
        return this.syncSendMessage(t, topicName, null, msgId, null);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, Integer timeout) {
        return this.syncSendMessage(t, topicName, null, null, timeout);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, String msgId) {
        return this.syncSendMessage(t, topicName, timestamp, msgId, null);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, Integer timeout) {
        return this.syncSendMessage(t, topicName, timestamp, null, timeout);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, String msgId, Integer timeout) {
        return this.syncSendMessage(t, topicName, null, msgId, timeout);
    }

    @Override
    public <T> SendResultFuture syncSendMessage(T t, String topicName, Long timestamp, String msgId, Integer timeout) {
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(timestamp);
        message.setBody(t);
        message.setMsgId(msgId);
        return this.syncSendMessage(message, timeout);
    }

    @Override
    public SendResultFuture syncSendMessage(Message<?> message) {
        return this.syncSendMessage(message, null);
    }

    @Override
    public SendResultFuture syncSendMessage(Message<?> message, Integer timeout) {
        String queueName = validateAndGetQueueNum(message);
        Future<SendResultFuture> submit = producerExecutor.submit(() -> {
            try {
                String msgId = this.pushMessageToQueue(queueName, message);
                return new SendResultFuture(true, msgId);
            } catch (Exception e) {
                return new SendResultFuture(false, e);
            }
        });
        SendResultFuture sendResultFuture = null;
        try {
            if (timeout != null) {
                sendResultFuture = submit.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                sendResultFuture = submit.get();
            }
        } catch (Exception e) {
            submit.cancel(true);
            return new SendResultFuture(false, e);
        }
        return sendResultFuture;
    }

    @Override
    public <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName) {
        return this.asyncSendMessageFuture(t, topicName, System.currentTimeMillis());
    }

    @Override
    public <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, Long timestamp) {
        Assert.notNull(timestamp, "'timestamp' must be not null");
        return this.asyncSendMessageFuture(t, topicName, timestamp, null);
    }

    @Override
    public <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, String msgId) {
        return this.asyncSendMessageFuture(t, topicName, System.currentTimeMillis(), msgId);
    }

    @Override
    public <T> Future<SendResultFuture> asyncSendMessageFuture(T t, String topicName, Long timestamp, String msgId) {
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(timestamp);
        message.setBody(t);
        message.setMsgId(msgId);
        return this.asyncSendMessageFuture(message);
    }

    @Override
    public Future<SendResultFuture> asyncSendMessageFuture(Message<?> message) {
        String queueName = validateAndGetQueueNum(message);
        Future<SendResultFuture> submit = producerExecutor.submit(() -> {
            try {
                String msgId = this.pushMessageToQueue(queueName, message);
                return new SendResultFuture(true, msgId);
            } catch (Exception e) {
                return new SendResultFuture(false, e);
            }
        });
        return submit;
    }

    @Override
    public <T> void asyncSendMessageCallback(T t, String topicName, SendResultCallback sendResultCallback) {
        this.asyncSendMessageCallback(t, topicName, System.currentTimeMillis(), sendResultCallback);
    }

    @Override
    public <T> void asyncSendMessageCallback(T t, String topicName, Long timestamp, SendResultCallback sendResultCallback) {
        this.asyncSendMessageCallback(t, topicName, timestamp, null, sendResultCallback);
    }

    @Override
    public <T> void asyncSendMessageCallback(T t, String topicName, String msgId, SendResultCallback sendResultCallback) {
        this.asyncSendMessageCallback(t, topicName, System.currentTimeMillis(), msgId, sendResultCallback);
    }

    @Override
    public <T> void asyncSendMessageCallback(T t, String topicName, Long timestamp, String msgId, SendResultCallback sendResultCallback) {
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(System.currentTimeMillis());
        message.setBody(t);
        message.setExecuteTime(timestamp);
        message.setMsgId(msgId);
        this.asyncSendMessageCallback(message, sendResultCallback);
    }

    @Override
    public <T> void asyncSendMessageCallback(Message<T> message, SendResultCallback sendResultCallback) {
        String queueName = validateAndGetQueueNum(message);
        CompletableFuture.runAsync(() -> {
            try {
                String msgId = this.pushMessageToQueue(queueName, message);
                sendResultCallback.success(msgId);
            } catch (Exception e) {
                sendResultCallback.fail(e);
            }
        });
    }

    @Override
    public <T> String sendTransactionMessage(T t, String topicName, Long timestamp) {
        Assert.notNull(timestamp, "'timestamp' must be not null");
        return this.sendTransactionMessage(t, topicName, timestamp, null);
    }

    @Override
    public <T> String sendTransactionMessage(T t, String topicName, String msgId) {
        return this.sendTransactionMessage(t, topicName, System.currentTimeMillis(), msgId);
    }

    @Override
    public <T> String sendTransactionMessage(T t, String topicName, Long timestamp, String msgId) {
        Message<T> message = new Message<>();
        message.setTopicName(topicName);
        message.setExecuteTime(timestamp);
        message.setBody(t);
        message.setMsgId(msgId);
        return this.sendTransactionMessage(message);
    }

    @Override
    public String sendTransactionMessage(Message<?> message) {
        validateAndCreateTopic(message);
        String msgId = messageOperate.pushTransactionMessageToQueue(message);
        TransactionListenerPhrase phrase = transactionListenerContainer.getDelayMQLocalTransactionListener(message, queueType);
        if (phrase == null) {
            throw new BizException("no such DelayMQLocalTransactionListener", null);
        }
        DelayMQTransactionState delayMQTransactionState = phrase.getDelayMQLocalTransactionListener().executeLocalTransaction(message.getBody(), msgId);
        if (delayMQTransactionState.equals(DelayMQTransactionState.COMMIT)) {
            messageOperate.commitTransactionMessageToQueue(message, msgId);
            phrase.getDelayMQLocalTransactionListener().afterCommitTransactionMessage(message.getBody(), msgId);
        } else if (delayMQTransactionState.equals(DelayMQTransactionState.ROLLBACK)) {
            messageOperate.rollbackTransactionMessageToQueue(msgId);
            phrase.getDelayMQLocalTransactionListener().afterRollbackTransactionMessage(message.getBody(), msgId);
        }
        return msgId;
    }

    /**
     * 入队
     *
     * @param queueName
     * @param message
     * @return
     */
    private String pushMessageToQueue(String queueName, Message<?> message) {
        String msgId;
        if (UtilAll.isNotBlank(message.getMsgId())) {
            msgId = message.getMsgId();
        } else {
            msgId = UUID.randomUUID().toString();
        }
        messageOperate.storeMessage(queueName, message, msgId);
        return msgId;
    }

    /**
     * 获取入队队列
     *
     * @param message
     * @return
     */
    private String validateAndGetQueueNum(Message<?> message) {
        TopicQueue topicQueue = validateAndCreateTopic(message);
        Rebalance rebalance = RebalanceStrategyEnum.match(topicQueue.getTopicQueueData().getRebalanceStrategyEnum(), RebalanceStrategyEnum.ROUND);
        return rebalance.getRebalancePushQueue(topicQueue);
    }

    /**
     * 获取topic信息 不存在即校验并创建topic
     *
     * @param message
     * @return
     */
    private TopicQueue validateAndCreateTopic(Message<?> message) {
        validateProducer();
        validateMessage(message);
        TopicQueue topicQueue = topicManager.getTopicQueue(message.getTopicName(), queueType, false);
        if (topicQueue == null) {
            System.out.println("2222" + JSONUtil.toJSONString(topicQueue));
            checkTopicIsAutoCreate(delayMQProperties);
            topicQueue = this.createTopicQueue(message.getTopicName(), delayMQProperties.getProducer());
            topicManager.updateTopicQueueTable(topicQueue, queueType);
            System.out.println("111" + JSONUtil.toJSONString(topicQueue));
        }
        return topicQueue;
    }

    /**
     * 是否已启动
     */
    private void validateProducer() {
        if (!isRunning()) {
            throw new BizException(ErrorCode.PRODUCER_NOT_START.getDescription(), null);
        }
    }

    /**
     * 验证消息
     *
     * @param message
     */
    protected void validateMessage(Message message) {
        if (UtilAll.isBlank(message.getTopicName())) {
            throw new BizException("check topic not null", null);
        }
    }

    /**
     * 确认是否可自动创建topic
     *
     * @param delayMQProperties
     */
    protected void checkTopicIsAutoCreate(DelayMQProperties delayMQProperties) throws BizException {
        if (!delayMQProperties.getProducer().isAutoCreatTopic()) {
            throw new BizException(ErrorCode.CANNOT_AUTO_CREATE_TOPIC.getDescription(), null);
        }
    }

    /**
     * 创建topic队列
     *
     * @param topicName
     * @param producer
     * @return
     */
    private TopicQueue createTopicQueue(String topicName, DelayMQProperties.Producer producer) {
        int writeTopicQueueNum = producer.getWriteTopicQueueNum();
        if (writeTopicQueueNum <= 0) {
            throw new BizException("property 'com.luoluo.delaymq.producer.write-topic-queue-num' must >0 ", null);
        }
        //todo 分布式加锁 解锁
        TopicQueue topicQueue = new TopicQueue();
        TopicQueueData topicQueueData = new TopicQueueData();
        List<String> queueNames = new ArrayList<>();
        for (int i = 0; i < writeTopicQueueNum; i++) {
            String queueName = TopicManager.getTopicMsgQueue(topicName, i);
            //建立队列
            messageOperate.createTopicQueue(queueName, queueName);
            String retryTopicName = TopicManager.getTopicMsgQueue(topicName + MQConstant.MSG_DELAY, i);
            this.createRetryTopicQueue(retryTopicName, retryTopicName);
            queueNames.add(queueName);
        }
        topicQueueData.setQueueNames(queueNames);
        //todo 更新rebalance策略
        topicQueueData.setRebalanceStrategyEnum(RebalanceStrategyEnum.ROUND);
        topicQueue.setTopicName(topicName);
        topicQueue.setTopicQueueData(topicQueueData);
        messageOperate.createTopicQueueData(topicName, JSONUtil.toJSONString(topicQueueData));
        return topicQueue;
    }

    /**
     * 创建重试队列
     *
     * @param retryTopicName
     * @param value
     */
    private void createRetryTopicQueue(String retryTopicName, String value) {
        messageOperate.createTopicQueue(retryTopicName, value);
    }

    /**
     * 根据hash获取入队队列
     *
     * @param message
     * @param hashkey
     * @return
     */
    private String validateAndHashGetQueueNum(Message<?> message, String hashkey) {
        TopicQueue topicQueue = validateAndCreateTopic(message);
        HashRebalanceImpl rebalance = (HashRebalanceImpl) RebalanceStrategyEnum.match(null, RebalanceStrategyEnum.HASH);
        return rebalance.getRebalancePushQueue(topicQueue, hashkey);
    }
}
