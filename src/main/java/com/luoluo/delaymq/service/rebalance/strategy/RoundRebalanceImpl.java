package com.luoluo.delaymq.service.rebalance.strategy;

import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.service.rebalance.Rebalance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName RoundRebalanceImpl
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
public class RoundRebalanceImpl implements Rebalance {

    private AtomicLong incr = new AtomicLong(0L);

    @Override
    public String getRebalancePushQueue(TopicQueue topicQueue) {
        int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
        long andIncrement = incr.getAndIncrement();
        int queueNum = (int) (andIncrement % queueSize);
        return TopicManager.getTopicMsgQueue(topicQueue.getTopicName(), queueNum);
    }
}
