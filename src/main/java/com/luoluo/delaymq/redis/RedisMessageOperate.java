package com.luoluo.delaymq.redis;

import com.luoluo.delaymq.common.*;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.mysql.ConsumerMsg;
import com.luoluo.delaymq.mysql.RecordConsumeTime;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.delaymq.utils.UtilAll;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @ClassName RedisMessageOperate
 * @Description: REDIS 消息操作类
 * @Author luoluo
 * @Date 2020/8/6
 * @Version V1.0
 **/
@Slf4j
public class RedisMessageOperate implements MessageOperate {

    private RedisUtils redisUtils;

    private int msgSurviveTime = 60 * 60 * 24;

    public void setMsgSurviveTime(int msgSurviveTime) {
        this.msgSurviveTime = msgSurviveTime;
    }

    public void setRedisUtils(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    public String get(String key) {
        return redisUtils.get(key);
    }

    @Override
    public String getMessage(String id) {
        return redisUtils.get(MQConstant.MSG_STORE + id);
    }

    @Override
    public String getTransactionMessage(String id) {
        return redisUtils.get(MQConstant.MSG_TRANSACTION_STORE + id);
    }

    @Override
    public ConsumerMsg getConsumerMsgDataClustering(String id, String consumerGroup) {
        String consumerMsgDataStr = redisUtils.hmGet(MQConstant.MSG_CONSUME + id, consumerGroup);
        if (UtilAll.isNotBlank(consumerMsgDataStr)) {
            ConsumerMsg consumerMsg = JSONUtil.parseObject(consumerMsgDataStr, ConsumerMsg.class);
            return consumerMsg;
        }
        return null;
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, long beginTimeStamp, long endTimeStamp) {
        return redisUtils.rangeByScore(topicName, beginTimeStamp, endTimeStamp);
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp) {
        return redisUtils.rangeByScore(TopicManager.getTopicMsgQueue(topicName, queueName), beginTimeStamp, endTimeStamp);
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp, int offset, int pullMessageSize) {
        return redisUtils.zrangeByScore(TopicManager.getTopicMsgQueue(topicName, queueName), beginTimeStamp, endTimeStamp, offset, pullMessageSize);
    }

    @Override
    public List<RecordConsumeTime> getRecordTopicConsumeTime(String consumerGroup, String topicName) {
        List<RecordConsumeTime> recordConsumeTimeList = new ArrayList<>();
        Map<String, String> map = redisUtils.hmGetAll(MQConstant.MESSAGE_TOPIC_CONSUME + topicName + ":" + consumerGroup);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            RecordConsumeTime recordConsumeTime = new RecordConsumeTime();
            recordConsumeTime.setConsumerGroup(consumerGroup);
            recordConsumeTime.setTopicName(topicName);
            recordConsumeTime.setQueueNum(Integer.parseInt(entry.getKey()));
            recordConsumeTime.setConsumeTime(Long.valueOf(entry.getValue()));
            recordConsumeTimeList.add(recordConsumeTime);
        }
        return recordConsumeTimeList;
    }

    @Override
    public TransactionMsgData getMessageTransactionRetryData(String id) {
        String value = redisUtils.get(MQConstant.MSG_TRANSACTION_RETRY + id);
        if (UtilAll.isNotBlank(value)) {
            TransactionMsgData transactionMsgData = new TransactionMsgData();
            transactionMsgData.setRetryCount(Integer.valueOf(value));
            return transactionMsgData;
        }
        return null;
    }

    @Override
    public void delayTopicQueueMessage(String topic, int queueName, String mgsId, int retryDelayTime) {
        redisUtils.zsset(TopicManager.getTopicMsgQueue(topic + MQConstant.MSG_DELAY, queueName), mgsId, System.currentTimeMillis() + retryDelayTime * DateNumUtil.SECOND);
    }

    @Override
    public void persistentConsumerMsgData(String id, String consumerGroup, ConsumerMsg consumerMsg) {
        String consumerMsgId = consumerMsg.getId();
        consumerMsg.setId(null);
        Long delayTime = Long.valueOf(msgSurviveTime);
        redisUtils.hmSet(MQConstant.MSG_CONSUME + consumerMsg.getTopic() + ":" + id, consumerGroup, JSONUtil.toJSONString(consumerMsg), delayTime);
        consumerMsg.setId(consumerMsgId);
    }

    @Override
    public void persistentMessage(String id, Message message) {
        long delayTime = getDelayTime(message);
        redisUtils.setExp(MQConstant.MSG_STORE + id, JSONUtil.toJSONString(message), delayTime);
    }

    @Override
    public void removeMsgIdInQueue(String topicName, int queue, String id) {
        String topicQueue = TopicManager.getTopicMsgQueue(topicName, queue);
        redisUtils.zdel(topicQueue, id);
    }

    @Override
    public void storeMessage(String topicQueue, Message message, String msgId) {
        Long delayTime = getDelayTime(message);
        redisUtils.setExp(MQConstant.MSG_STORE + msgId, JSONUtil.toJSONString(message.getBody()), delayTime);
        redisUtils.zsset(topicQueue, msgId, message.getExecuteTime());
        ConsumerMsgData consumerMsgData = new ConsumerMsgData();
        consumerMsgData.setCreatedTime(new Date());
        consumerMsgData.setExecuteTime(new Date(message.getExecuteTime()));
        redisUtils.hmSet(MQConstant.MSG_CONSUME + message.getTopicName() + ":" + msgId, msgId, JSONUtil.toJSONString(consumerMsgData), delayTime);
    }

    @Override
    public void storeTransactionMessage(String topicQueue, Message message, String msgId) {
        Long delayTime = getDelayTime(message);
        redisUtils.setExp(MQConstant.MSG_TRANSACTION_STORE + msgId, JSONUtil.toJSONString(message), delayTime);
        redisUtils.zsset(topicQueue, msgId, System.currentTimeMillis());
        redisUtils.set(MQConstant.MSG_TRANSACTION_RETRY + msgId, String.valueOf(0));
    }

    @Override
    public void createTopicQueue(String queueName, String value) {
        redisUtils.zsset(queueName, value, Long.MAX_VALUE);
    }

    @Override
    public void createTopicQueueData(String topicName, String topicData) {
        redisUtils.set(MQConstant.MESSAGE_TOPIC_TABLE + topicName, topicData);
    }

    @Override
    public void recordTopicConsumeTime(String topic, String consumerGroup, int queueNum, long newBeginTimeStamp) {
        redisUtils.hmSet(MQConstant.MESSAGE_TOPIC_CONSUME + topic + ":" + consumerGroup, String.valueOf(queueNum), String.valueOf(newBeginTimeStamp));
    }

    @Override
    public void delete(String msgId) {
        redisUtils.remove(msgId);
    }

    @Override
    public void setTopicMessage(String topicMsgQueue, String msgId, long executeTime) {
        redisUtils.zsset(topicMsgQueue, msgId, executeTime);
    }

    @Override
    public boolean deleteTopicMessage(String topicQueueName, String key) {
        return redisUtils.zdel(topicQueueName, key);
    }

    @Override
    public void commitTransactionMessageToQueue(Message<?> message, String msgId) {
        this.storeMessage(TopicManager.getTopicMsgQueue(message.getTopicName(), 0), message, msgId);
        this.deleteTopicMessage(MQConstant.TRANSACTION_GLOBAL_NAME, msgId);
        this.delete(MQConstant.MSG_TRANSACTION_STORE + msgId);
        this.delete(MQConstant.MSG_TRANSACTION_RETRY + msgId);
    }

    @Override
    public void rollbackTransactionMessageToQueue(String msgId) {
        this.deleteTopicMessage(MQConstant.TRANSACTION_GLOBAL_NAME, msgId);
        this.delete(MQConstant.MSG_TRANSACTION_STORE + msgId);
        this.delete(MQConstant.MSG_TRANSACTION_RETRY + msgId);
    }

    @Override
    public String pushTransactionMessageToQueue(Message<?> message) {
        String msgId = UUID.randomUUID().toString();
        this.storeTransactionMessage(MQConstant.TRANSACTION_GLOBAL_NAME, message, msgId);
        return msgId;
    }

    @Override
    public String getTopicQueue(String topicName) {
        return this.get(MQConstant.MESSAGE_TOPIC_TABLE + topicName);
    }

    @Override
    public void delayTransactionMessageToQueue(String msgId, long delayTime, TransactionMsgData transactionMsgData) {
        redisUtils.zsset(MQConstant.TRANSACTION_GLOBAL_NAME, msgId, System.currentTimeMillis() + delayTime);
        transactionMsgData.setRetryCount(transactionMsgData.getRetryCount() + 1);
        redisUtils.set(MQConstant.MSG_TRANSACTION_RETRY + msgId, String.valueOf(transactionMsgData.getRetryCount()));
    }

    private long getDelayTime(Message<?> message) {
        return ((message.getExecuteTime() - System.currentTimeMillis()) / 1000) + message.getTtl() + msgSurviveTime;
    }

}
