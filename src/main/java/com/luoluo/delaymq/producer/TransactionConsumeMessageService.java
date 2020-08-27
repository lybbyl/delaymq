package com.luoluo.delaymq.producer;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.common.MessageOperate;
import com.luoluo.delaymq.common.TransactionMsgData;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.delaymq.utils.ThreadFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName TransactionConsumeMessageService
 * @Description: 事务消费服务
 * @Author luoluo
 * @Date 2020/8/6
 * @Version V1.0
 **/
@Slf4j
public class TransactionConsumeMessageService implements SmartLifecycle, Runnable {

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    /**
     * 操作队列
     */
    protected MessageOperate messageOperate;

    /**
     * 操作队列类型
     */
    protected QueueTypeEnum queueType;

    /**
     * 启动标识
     */
    private boolean running = false;

    /**
     * 一个consumerGroup 对应一个扫描线程 n个调度线程
     */
    private final ScheduledExecutorService scanConsumerSchedule = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "scanTransactionConsumerSchedule", true));

    public TransactionConsumeMessageService(DistributedLock distributedLock, MessageOperate messageOperate, QueueTypeEnum queueType) {
        this.distributedLock = distributedLock;
        this.messageOperate = messageOperate;
        this.queueType = queueType;
    }

    /**
     * 事务消息调度启动
     */
    @Override
    public void start() {
        scanConsumerSchedule.scheduleAtFixedRate(this, 1000 * 3, 1000, TimeUnit.MILLISECONDS);
        running = true;
    }

    /**
     * 关闭
     */
    @Override
    public void stop() {
        if (isRunning()) {
            scanConsumerSchedule.shutdownNow();
            running = false;
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * run
     */
    @Override
    public void run() {
        try {
            consumeTransactionTopicMessage();
        } catch (Throwable t) {
            log.info("schedule transaction message fail", t);
        }
    }

    /**
     * 拉取事务消息并消费
     */
    public void consumeTransactionTopicMessage() {
        Collection<String> keys = messageOperate.pullMessageFromBeginToEnd(MQConstant.TRANSACTION_GLOBAL_NAME, 0, System.currentTimeMillis());
        for (String id : keys) {
            consumeTransactionSingleMessage(id);
        }
    }

    /**
     * 1: 锁消息
     * 2：确认消息是否提交或者回滚
     * 3：失败则滞后消息 等待重试
     * 4：超过次数尝试后 不再重试
     *
     * @param id
     */
    private void consumeTransactionSingleMessage(String id) {
        String lockKey = MQConstant.MSG_LOCK_POOL + MQConstant.TRANSACTION_GLOBAL_NAME + ":" + id;
        if (!distributedLock.tryLock(lockKey)) {
            log.info("other applications are processing,transactionId:{}", id);
            return;
        }
        try {
            String messageValue = messageOperate.getTransactionMessage(id);
            if (messageValue == null) {
                //消息为空 则 移除队列消息
                messageOperate.rollbackTransactionMessageToQueue(id);
                return;
            }
            TransactionMsgData transactionMsgData = messageOperate.getMessageTransactionRetryData(id);
            if (transactionMsgData == null) {
                transactionMsgData = new TransactionMsgData(0);
            }
            Message message = JSONUtil.parseObject(messageValue, Message.class);
            TransactionListenerPhrase transactionListenerPhrase = TransactionListenerContainer.getInstance().getDelayMQLocalTransactionListener(message, queueType);
            Object body = parseMessageType(message, (Class) transactionListenerPhrase.getMessageType());
            DelayMQLocalTransactionListener delayMQLocalTransactionListener = null;
            try {
                if (transactionListenerPhrase != null) {
                    delayMQLocalTransactionListener = transactionListenerPhrase.getDelayMQLocalTransactionListener();
                    DelayMQTransactionState delayMQTransactionState = delayMQLocalTransactionListener.checkLocalTransaction(body,id);
                    if (DelayMQTransactionState.COMMIT.equals(delayMQTransactionState)) {
                        log.info("commit message,id:{}", id);
                        messageOperate.commitTransactionMessageToQueue(message, id);
                        delayMQLocalTransactionListener.afterCommitTransactionMessage(body,id);
                    } else if (DelayMQTransactionState.ROLLBACK.equals(delayMQTransactionState)) {
                        log.info("rollback message,id:{}", id);
                        messageOperate.rollbackTransactionMessageToQueue(id);
                        delayMQLocalTransactionListener.afterRollbackTransactionMessage(body,id);
                    } else if (DelayMQTransactionState.UNKNOWN.equals(delayMQTransactionState)) {
                        throw new BizException("unsure message status", null);
                    }
                }
            } catch (Exception e) {
                if (transactionMsgData.getRetryCount() >= 5) {
                    log.warn("rollback transaction Message after over retry count, id:{}", id);
                    messageOperate.rollbackTransactionMessageToQueue(id);
                    delayMQLocalTransactionListener.afterOverRetryTransactionMessage(body,id);
                    return;
                } else {
                    log.info("delay Message,id:{}", id);
                    messageOperate.delayTransactionMessageToQueue(id, DateNumUtil.FIVE_SECOND, transactionMsgData);
                    delayMQLocalTransactionListener.afterDelayTransactionMessage(body,id);
                    return;
                }
            }
        } finally {
            //解锁
            distributedLock.unlock(lockKey);
        }
        return;
    }

    private <T> T parseMessageType(Message message, Class<T> messageType) {
        return JSONUtil.parseObject(message.getBody(),messageType);
    }
}
