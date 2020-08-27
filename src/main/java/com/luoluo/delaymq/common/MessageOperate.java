package com.luoluo.delaymq.common;

import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.mysql.ConsumerMsg;
import com.luoluo.delaymq.mysql.RecordConsumeTime;

import java.util.Collection;
import java.util.List;

/**
 * @Date: 2020/8/6 18:03
 * @Author: luoluo
 * @Description: 消息操作接口 目前支持 REDIS MYSQL
 */
public interface MessageOperate {

    /**
     * 获取对应key的值
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 根据id 获取对应的消息
     * @param id
     * @return
     */
    String getMessage(String id);

    /**
     * 根据id 获取对应的事务消息
     * @param id
     * @return
     */
    String getTransactionMessage(String id);

    /**
     * 集群模式下获取消息消费状态
     * ps: 集群模式消费情况将会记录在对应队列的消费表中
     *
     * @param id
     * @param topic
     * @param consumerGroup
     * @return
     */
    ConsumerMsg getConsumerMsgDataClustering(String id,String topic, String consumerGroup);

    /**
     * 拉取topic中所有的key 按照给定范围排序
     * ps: 该topic不可分割多个队列 即一个topic对应该队列
     * @param topicName
     * @param beginTimeStamp
     * @param endTimeStamp
     * @return
     */
    Collection<String> pullMessageFromBeginToEnd(String topicName, long beginTimeStamp, long endTimeStamp);

    /**
     * 拉取topic中对应queue中所有的key 按照给定范围排序
     * @param topicName
     * @param queueName
     * @param beginTimeStamp
     * @param endTimeStamp
     * @return
     */
    Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp);

    /**
     * 拉取topic中对应queue中key 最大pullMessageSize条数 按照给定范围排序
     * @param topicName
     * @param queueName
     * @param beginTimeStamp
     * @param endTimeStamp
     * @return
     */
    Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp, int offset, int pullMessageSize);

    /**
     * 存储事务消息
     * @param topicQueue
     * @param message
     * @param msgId
     */
    void storeTransactionMessage(String topicQueue, Message message, String msgId);

    /**
     * 获取事务消息状态
     * @param id
     * @return
     */
    TransactionMsgData getMessageTransactionRetryData(String id);

    /**
     * 获取对应topic值
     *
     * @param topicName
     * @return
     */
    String getTopicQueue(String topicName);

    /**
     * 持久化消息
     * @param id
     * @param message
     */
    void persistentMessage(String id, Message message);

    /**
     * 删除消息
     * @param msgId
     */
    void delete(String msgId);

    /**
     * 从指定topic和queue中 移除某个key
     * @param topicName
     * @param queue
     * @param id
     */
    void removeMsgIdInQueue(String topicName, int queue, String id);

    /**
     * 存储消息 redis中需要用delayTime设置ttl
     * @param topicQueue
     * @param message
     * @param msgId
     */
    void storeMessage(String topicQueue, Message message, String msgId);

    /**
     * 创建topic及对应的Queue
     * @param queueName
     * @param value
     */
    void createTopicQueue(String queueName, String value);

    /**
     * 添加key到topic中
     * @param topicMsgQueue
     * @param msgId
     * @param executeTime
     */
    void setTopicMessage(String topicMsgQueue, String msgId, long executeTime);

    /**
     * 从topic中移除key
     * @param topicQueueName
     * @param key
     * @return
     */
    boolean deleteTopicMessage(String topicQueueName, String key);

    /**
     * 确认事务消息
     * @param message
     * @param msgId
     */
    void commitTransactionMessageToQueue(Message<?> message, String msgId);

    /**
     * 回滚事务消息
     * @param msgId
     */
    void rollbackTransactionMessageToQueue(String msgId);

    /**
     * 事务消息延时
     * @param msgId
     * @param delayTime
     * @param transactionMsgData
     */
    void delayTransactionMessageToQueue(String msgId, long delayTime, TransactionMsgData transactionMsgData);

    /**
     * 推送事务消息入队
     * @param message
     * @return
     */
    String pushTransactionMessageToQueue(Message<?> message);

    /**
     * 持久化消息消费状态
     * @param id
     * @param consumerGroup
     * @param consumerMsgData
     */
    void persistentConsumerMsgData(String id, String consumerGroup, ConsumerMsg consumerMsgData);

    /**
     * 消息延时
     * @param topic
     * @param queueName
     * @param mgsId
     * @param retryDelayTime
     */
    void delayTopicQueueMessage(String topic, int queueName, String mgsId, int retryDelayTime);


    /**
     * 创建topic
     * @param topicName
     * @param toJSONString
     */
    void createTopicQueueData(String topicName, String toJSONString);

    /**
     * getRecordTopicConsumeTime
     * @param topicName
     */
    List<RecordConsumeTime> getRecordTopicConsumeTime(String consumerGroup, String topicName);

    /**
     *
     * @param topic
     * @param consumerGroup
     * @param queueNum
     * @param newBeginTimeStamp
     */
    void recordTopicConsumeTime(String topic, String consumerGroup, int queueNum, long newBeginTimeStamp);
}
