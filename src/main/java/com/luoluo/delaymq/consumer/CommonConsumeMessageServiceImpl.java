package com.luoluo.delaymq.consumer;

import com.luoluo.delaymq.common.*;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.ConsumeMode;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.exception.BizException;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.mysql.ConsumerMsg;
import com.luoluo.delaymq.mysql.RecordConsumeTime;
import com.luoluo.delaymq.producer.DelayMQLocalTransactionListener;
import com.luoluo.delaymq.redis.RedisUtils;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.delaymq.utils.JSONUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.MethodParameter;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.luoluo.delaymq.common.SpringContext.getBean;

/**
 * @ClassName CommonConsumeMessageService
 * @Description: 执行消费消息的核心实现类
 * @Author luoluo
 * @Date 2020/7/10
 * @Version V1.0
 **/
@Slf4j
public class CommonConsumeMessageServiceImpl implements ConsumeMessageService {

    /**
     * 锁
     */
    protected DistributedLock distributedLock;

    /**
     * 监听bean
     */
    protected DelayMQConsumerListener delayMQConsumerListener;

    /**
     * 消费者组
     */
    protected String consumerGroup;

    //
    /**
     * topic
     */
    protected String topic;

    /**
     * 回溯时间片
     */
    protected int backTrackTime;

    /**
     * 注解监听值
     */
    protected DelayMQMessageListener anno;

    /**
     * 消费者配置
     */
    protected DelayMQProperties.Consumer consumerProperties;

    /**
     *
     */
    protected ConsumeMode consumeMode;
    protected long consumeTimeout;
    @Setter
    protected int retryDelayTime;
    @Setter
    protected int retryCount;
    protected int pullMessageSize;
    protected boolean supportTransaction;

    /**
     * 回溯比对key
     */
    private BackTrackCacheValues<String> backTrackValues;

    /**
     * 消息类型
     */
    protected Type messageType;

    /**
     *
     */
    protected MethodParameter methodParameter;

    /**
     * count todo
     */
    private AtomicInteger counter = new AtomicInteger(0);

    /**
     * 消息消费
     */
    protected MessageOperate messageOperate;

    /**
     * 如果是事务 则需要使用spring代理类
     */
    private CommonConsumeMessageServiceImpl consumeMessageServiceProxy;

    /**
     * 消费类的名称 delayMQConsumerListener
     */
    private String delayMQConsumerListenerName;

    private RedisUtils redisUtils;

    /**
     * 获取spring代理类
     *
     * @return
     */
    @Override
    public void init(String name) {
        if (supportTransaction) {
            consumeMessageServiceProxy = getBean(name);
        } else {
            consumeMessageServiceProxy = this;
        }
        redisUtils = getBean(RedisUtils.class);
    }

    @Override
    public List<RecordConsumeTime> getRecordTopicConsumeTime(String consumerGroup, String topicName) {
        return messageOperate.getRecordTopicConsumeTime(consumerGroup, topicName);
    }

    /**
     * 初始化 广播模式写文件
     *
     * @param consumerGroup
     * @param topic
     * @param anno
     * @param consumerProperties
     * @param distributedLock
     * @param delayMQConsumerListener
     */
    public CommonConsumeMessageServiceImpl(String consumerGroup, String topic, DelayMQMessageListener anno, DelayMQProperties.Consumer consumerProperties, DistributedLock distributedLock, DelayMQConsumerListener delayMQConsumerListener) {
        this.delayMQConsumerListener = delayMQConsumerListener;
        this.delayMQConsumerListenerName = AopProxyUtils.ultimateTargetClass(delayMQConsumerListener).getName();
        this.distributedLock = distributedLock;
        this.consumerGroup = consumerGroup;
        this.topic = topic;

        this.consumerProperties = consumerProperties;
        this.anno = anno;
        this.consumeMode = anno.consumeMode();

        this.consumeTimeout = anno.consumeTimeout();
        if (consumeTimeout == Integer.MIN_VALUE) {
            consumeTimeout = consumerProperties.getConsumeTimeout();
        }
        if (consumeTimeout <= MQConstant.CONSUME_ZERO_SIZE) {
            consumeTimeout = MQConstant.DEFAULT_CONSUME_TIME_OUT;
        }

        this.pullMessageSize = anno.pullMessageSize();
        if (pullMessageSize == Integer.MIN_VALUE) {
            pullMessageSize = consumerProperties.getPullMessageSize();
        }
        if (pullMessageSize <= MQConstant.CONSUME_ZERO_SIZE) {
            pullMessageSize = MQConstant.DEFAULT_CONSUME_PULL_MESSAGE_SIZE;
        }

        this.supportTransaction = anno.supportTransaction();
        if (!supportTransaction) {
            supportTransaction = consumerProperties.isSupportTransaction();
        }

        this.backTrackTime = anno.backTrackTime();
        if (backTrackTime == Integer.MIN_VALUE) {
            backTrackTime = consumerProperties.getBackTrackTime();
        }
        if (backTrackTime <= MQConstant.CONSUME_ZERO_SIZE) {
            //目前一定需要回溯消息 暂定为3s
            backTrackTime = MQConstant.DEFAULT_CONSUME_BACK_TRACK_TIME;
        }
        backTrackValues = new BackTrackCacheValues<>(backTrackTime);

        this.messageType = getMessageType();
        this.methodParameter = getMethodParameter();

    }

    /**
     * 设置操作类型
     *
     * @param messageOperate
     */
    public void setMessageOperate(MessageOperate messageOperate) {
        this.messageOperate = messageOperate;
    }

    /**
     * 回溯topic
     */
    @Override
    public void backTrackTopicMessage(String topicName, int queueNum, long oldBeginTimeStamp, long newBeginTimeStamp, int offset) {
        if (backTrackValues != null) {
            List<ConsumerMsg> consumeKeys = new ArrayList<>(256);
            backTrackTopicMessage(topicName, queueNum, oldBeginTimeStamp, newBeginTimeStamp, offset, consumeKeys);
            backTrackValues.addValue(consumeKeys.stream().map(ConsumerMsg::getMsgId).collect(Collectors.toList()));
        }

    }

    /**
     * 递归回溯
     *
     * @param topicName
     * @param queueNum
     * @param oldBeginTimeStamp
     * @param newBeginTimeStamp
     * @param offset
     * @param consumeKeys
     */
    public void backTrackTopicMessage(String topicName, int queueNum, long oldBeginTimeStamp, long newBeginTimeStamp, int offset, List<ConsumerMsg> consumeKeys) {
        if (backTrackValues != null) {
            Collection<String> keys = messageOperate.pullMessageFromBeginToEnd(topicName, queueNum, oldBeginTimeStamp - backTrackTime * DateNumUtil.SECOND, newBeginTimeStamp - DateNumUtil.SECOND, offset, pullMessageSize);
            int oldSize = keys.size();
            Set<String> backTrackSetIds = backTrackValues.getAllValue().stream().collect(Collectors.toSet());
            keys.removeAll(backTrackSetIds);
            consumeKeysMessage(topicName, queueNum, oldBeginTimeStamp, keys, consumeKeys, false);
            if (oldSize == pullMessageSize) {
                backTrackTopicMessage(topicName, queueNum, oldBeginTimeStamp, newBeginTimeStamp, offset + pullMessageSize, consumeKeys);
            }
        }

    }

    /**
     * 消费重试队列
     *
     * @param topic
     * @param queueNum
     * @param oldBeginTimeStamp
     * @param newBeginTimeStamp
     * @param i
     */
    @Override
    public void consumeRetryTopicMessage(String topic, int queueNum, long oldBeginTimeStamp, long newBeginTimeStamp, int i) {
        consumeTopicMessage(topic, queueNum, oldBeginTimeStamp, newBeginTimeStamp, i, true);
    }

    @Override
    public void recordTopicConsumeTime(int queueNum, long newBeginTimeStamp) {
        messageOperate.recordTopicConsumeTime(topic, consumerGroup, queueNum, newBeginTimeStamp);
    }

    /**
     * 消费队列
     *
     * @param topicName
     * @param queue
     * @param beginTimeStamp
     * @param endTimeStamp
     * @param offset
     * @param retry
     * @return
     */
    @Override
    public long consumeTopicMessage(String topicName, int queue, long beginTimeStamp, long endTimeStamp, int offset, boolean retry) {
        List<ConsumerMsg> consumeKeys = new ArrayList<>(256);
        consumeTopicMessage(topicName, queue, beginTimeStamp, endTimeStamp, offset, consumeKeys, retry);
        if (backTrackValues != null) {
            backTrackValues.addValue(consumeKeys.stream().map(ConsumerMsg::getMsgId).collect(Collectors.toList()));
        }
        return endTimeStamp + 1;
    }

    /**
     * 递归消费
     *
     * @param topicName
     * @param queue
     * @param beginTimeStamp
     * @param endTimeStamp
     * @param offset
     * @param consumeKeys
     * @return
     */
    public void consumeTopicMessage(String topicName, int queue, long beginTimeStamp, long endTimeStamp, int offset, List<ConsumerMsg> consumeKeys, boolean retry) {
        Collection<String> keys = messageOperate.pullMessageFromBeginToEnd(topicName, queue, beginTimeStamp, endTimeStamp, offset, pullMessageSize);
        consumeKeysMessage(topicName, queue, beginTimeStamp, keys, consumeKeys, retry);
        if (keys.size() == pullMessageSize) {
            consumeTopicMessage(topicName, queue, beginTimeStamp, endTimeStamp, offset + pullMessageSize, consumeKeys, retry);
        }
    }

    private void consumeKeysMessage(String topicName, int queue, long beginTimeStamp, Collection<String> keys, List<ConsumerMsg> consumeKeys, boolean retry) {
        long newBeginTimeStamp = beginTimeStamp;
        if (keys != null && keys.size() > 0) {
            for (String id : keys) {
                consumeMessageServiceProxy.consumeSingleMessage(topicName, queue, newBeginTimeStamp, consumeKeys, id, retry);
            }
        }
    }

    /**
     * 单条消费
     *
     * @param topicName
     * @param queue
     * @param newBeginTimeStamp
     * @param consumeKeys
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public void consumeSingleMessage(String topicName, int queue, long newBeginTimeStamp, List<ConsumerMsg> consumeKeys, String id, boolean retry) {
        String lockKey = MQConstant.MSG_LOCK_POOL + TopicManager.getTopicMsgQueue(topicName, queue) + ":" + anno.queueType() + ":" + consumerGroup + ":" + id;
        lockKey = lockKey.replace(MQConstant.MSG_DELAY, "");
        if (!distributedLock.tryLock(lockKey)) {
            log.info("other applications are processing,taskId:{},consumer:{}", id, delayMQConsumerListenerName);
            return;
        }
        try {
            String messageValue = messageOperate.getMessage(id);
            if (messageValue == null) {
                //消息为空 则 移除队列消息
                messageOperate.removeMsgIdInQueue(topicName, queue, id);
            }
            ConsumerMsg consumerMsg = messageOperate.getConsumerMsgDataClustering(id, consumerGroup);
            if (consumerMsg != null) {
                //如果已消费成功或完全失败 则跳过此条消息
                if (ConsumerStatus.SUCCESS.getDescription().equals(consumerMsg.getConsumerStatus()) || ConsumerStatus.COMPLETE_FAIL.getDescription().equals(consumerMsg.getConsumerStatus()) || System.currentTimeMillis() < consumerMsg.getRetryNextTime().getTime()) {
                    consumeKeys.add(consumerMsg);
                    return;
                }
            } else {
                consumerMsg = new ConsumerMsg();
                consumerMsg.setMsgId(id);
                consumerMsg.setRetryCount(0);
                consumerMsg.setCreatedTime(new Date());
                consumerMsg.setConsumerGroup(consumerGroup);
                consumerMsg.setTopic(topic);
            }
            Object messageBody = parseMessageType(messageValue, (Class) messageType);
            try {
                ConsumerStatus consumerStatus;
                if (supportTransaction) {
                    consumerStatus = delayMQConsumerListener.onTransactionMessage(messageBody, id);
                } else {
                    consumerStatus = delayMQConsumerListener.onMessage(messageBody, id);
                }
                //如果成功消费
                if (ConsumerStatus.SUCCESS.equals(consumerStatus)) {
                    updateConsumerMsgData(id, consumerMsg, ConsumerStatus.SUCCESS);
                    consumeKeys.add(consumerMsg);
                    delayMQConsumerListener.extraOperationAfterMessageSuccess(messageBody, id);
                    return;
                } else {
                    throw new BizException("Unsuccessful consumption");
                }
            } catch (Exception e) {
                log.info("Unsuccessful consumption" + Thread.currentThread().getId(), e);
                //重试超过上限
                if (consumerMsg != null && consumerMsg.getRetryCount() >= retryCount - 1) {
                    log.error("consumption completely failed", e);
                    updateConsumerMsgData(id, consumerMsg, ConsumerStatus.COMPLETE_FAIL);
                    consumeKeys.add(consumerMsg);
                    delayMQConsumerListener.extraOperationAfterMessageCompleteFail(messageBody, id);
                    return;
                }
                //集群模式消费消息才会重试
                messageOperate.delayTopicQueueMessage(topic, queue, id, retryDelayTime);

                updateConsumerMsgData(id, consumerMsg, ConsumerStatus.FAIL);
                //推动时间片
                consumeKeys.add(consumerMsg);
                delayMQConsumerListener.extraOperationAfterMessageFail(messageBody, id);
                return;
            }
        } catch (Exception e) {
            // 不应该都到这里 上述出现任何异常
            log.error("not expected Exception", e);
        } finally {
            //解锁
            distributedLock.unlock(lockKey);
        }
        return;
    }

    /**
     * 反序列化Message
     *
     * @param messageValue
     * @param messageType
     * @param <T>
     * @return
     */
    private <T> T parseMessageType(String messageValue, Class<T> messageType) {
        return JSONUtil.parseObject(messageValue, messageType);
    }

    /**
     * 获取messageType的字节码
     * 反射对应监听方法
     * 构造MethodParameter
     *
     * @return
     */
    private MethodParameter getMethodParameter() {
        Class<?> targetClass;
        targetClass = AopProxyUtils.ultimateTargetClass(delayMQConsumerListener);
        Type messageType = this.messageType;
        Class clazz = null;
        if (messageType instanceof ParameterizedType) {
            clazz = (Class) ((ParameterizedType) messageType).getRawType();
        } else if (messageType instanceof Class) {
            clazz = (Class) messageType;
        } else {
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
        try {
            // 反射对应监听方法
            final Method method = targetClass.getMethod("onMessage", clazz, String.class);
            // 构造MethodParameter
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
    }

    /**
     * class Demo01AConsumer implements DelayMQListener<MessageExt>
     * 解析出MessageExt
     *
     * @return
     */
    private Type getMessageType() {
        Class<?> targetClass;
        // 获取原始class Demo01AConsumer implements DelayMQListener<MessageExt>
        targetClass = AopProxyUtils.ultimateTargetClass(delayMQConsumerListener);
        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            //找Demo01AConsumer 实现的所有接口
            //返回实现接口信息的Type数组，包含泛型信息
            Type type = targetClass.getGenericSuperclass();
            if (Objects.nonNull(type)) {
                //找到DelayMQListener<MessageExt>
                if (type instanceof ParameterizedType &&
                        (Objects.equals(((ParameterizedType) type).getRawType(), AbstractDelayMQConsumerListener.class))) {
                    matchedGenericInterface = type;
                    break;
                }
            }
            //如果没有则继续找父类 直到没有为止
            targetClass = targetClass.getSuperclass();
        }
        //如果啥都没有 则直接返回Object
        if (!Objects.isNull(matchedGenericInterface)) {
            Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
            if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
                return actualTypeArguments[0];
            }
        }
        targetClass = AopProxyUtils.ultimateTargetClass(delayMQConsumerListener);
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

    private void updateMessage(String id, Message message) {
        messageOperate.persistentMessage(id, message);
    }

    private void updateConsumerMsgData(String id, ConsumerMsg consumerMsg, ConsumerStatus consumerStatus) {
        consumerMsg.setConsumerStatus(consumerStatus.getDescription());
        if (ConsumerStatus.SUCCESS.equals(consumerStatus) || ConsumerStatus.COMPLETE_FAIL.equals(consumerStatus)) {
            consumerMsg.setRetryNextTime(null);
            consumerMsg.setConsumerTime(new Date());
        } else {
            consumerMsg.setRetryCount(consumerMsg.getRetryCount() + 1);
            consumerMsg.setRetryNextTime(new Date(System.currentTimeMillis() + retryDelayTime * DateNumUtil.SECOND));
        }
        messageOperate.persistentConsumerMsgData(id, consumerGroup, consumerMsg);

    }

    /**
     * 队尾追加写入
     *
     * @param consumerMsg
     * @throws IOException
     */
    private void fileAppendConsumerMsgData(ConsumerMsg consumerMsg) throws IOException {
//        if (file.length() == 0) {
//            List<ConsumerMsg> consumerMsgDataList = new ArrayList<>();
//            consumerMsgDataList.add(consumerMsg);
//            file.write(JSONUtil.toJSONBytes(consumerMsgDataList));
//        } else {
//            String json = "," + JSONUtil.toJSONString(consumerMsg) + "]";
//            file.seek(file.length() - 1);
//            file.write(json.getBytes());
//        }
    }
}
