package com.luoluo.delaymq.service.rebalance;

import com.luoluo.delaymq.common.TopicQueue;

/**
 * @Date: 2020/7/9 9:48
 * @Author: luoluo
 * @Description: 负载接口
 */
public interface Rebalance {

    /**
     * 负载入队
     * @param topicQueueData
     * @return
     */
    String getRebalancePushQueue(TopicQueue topicQueueData);
}
