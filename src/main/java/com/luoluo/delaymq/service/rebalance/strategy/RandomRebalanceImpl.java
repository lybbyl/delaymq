package com.luoluo.delaymq.service.rebalance.strategy;

import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.service.rebalance.Rebalance;

/**
 * @ClassName RebalanceImpl
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
public class RandomRebalanceImpl implements Rebalance {

    @Override
    public String getRebalancePushQueue(TopicQueue topicQueueData) {
        return null;
    }
}
