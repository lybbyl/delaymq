package com.luoluo.delaymq.service.rebalance.strategy;

import com.luoluo.delaymq.service.rebalance.Rebalance;
import com.luoluo.delaymq.common.HashFunction;
import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TopicQueue;
import com.luoluo.delaymq.exception.BizException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName HashRebanceImpl
 * @Description: hash策略
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
public class HashRebalanceImpl implements Rebalance {

    private final HashFunction hashFunction = new MD5Hash();

    /**
     * 根据hashkey 负载
     *
     * @param topicQueue
     * @param hashKey
     * @return
     */
    public String getRebalancePushQueue(TopicQueue topicQueue, String hashKey) {
        int queueSize = topicQueue.getTopicQueueData().getQueueNames().size();
        int queueNum = (int) (hashFunction.hash(hashKey) % queueSize);
        return TopicManager.getTopicMsgQueue(topicQueue.getTopicName(), queueNum);
    }

    /**
     * 行为发生变化 不实现此方法 重新定义
     *
     * @param topicQueueData
     * @return
     */
    @Override
    public String getRebalancePushQueue(TopicQueue topicQueueData) {
        //todo check hashkey
        throw new BizException("cannot do it", null);
    }

    /**
     * default hash function
     */
    private static class MD5Hash implements HashFunction {
        MessageDigest instance;

        public MD5Hash() {
            try {
                instance = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
            }
        }

        //返回节点的hash值
        @Override
        public long hash(String key) {
            instance.reset();
            instance.update(key.getBytes());
            byte[] digest = instance.digest();

            long h = 0;
            for (int i = 0; i < 4; i++) {
                h <<= 8;
                h |= ((int) digest[i]) & 0xFF;
            }
            return h;
        }
    }
}
