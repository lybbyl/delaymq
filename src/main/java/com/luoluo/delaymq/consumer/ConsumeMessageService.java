package com.luoluo.delaymq.consumer;

import com.luoluo.delaymq.mysql.RecordConsumeTime;

import java.util.List;

/**
 * 消费消息执行接口
 *
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public interface ConsumeMessageService {

    /**
     * init
     */
    void init(String name);

    /**
     *
     * @param consumerGroup
     * @param topicName
     * @return
     */
    List<RecordConsumeTime> getRecordTopicConsumeTime(String consumerGroup, String topicName);

    /**
     * 消费消息
     *
     * @param topicName
     * @param queue
     * @param startTimeStamp
     * @param endTimeStamp
     * @param offset
     * @param b
     * @return
     */
    long consumeTopicMessage(String topicName, int queue, long startTimeStamp, long endTimeStamp, int offset, boolean b);

    /**
     * @param topic
     * @param queueNum
     * @param oldBeginTimeStamp
     * @param newBeginTimeStamp
     * @param offset
     */
    void backTrackTopicMessage(String topic, int queueNum, long oldBeginTimeStamp, long newBeginTimeStamp, int offset);

    /**
     * @param topic
     * @param queueNum
     * @param oldBeginTimeStamp
     * @param newBeginTimeStamp
     * @param i
     */
    void consumeRetryTopicMessage(String topic, int queueNum, long oldBeginTimeStamp, long newBeginTimeStamp, int i);

    /**
     *
     * @param queueNum
     * @param newBeginTimeStamp
     */
    void recordTopicConsumeTime(int queueNum, long newBeginTimeStamp);
}
