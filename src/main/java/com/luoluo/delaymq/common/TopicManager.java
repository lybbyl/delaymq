package com.luoluo.delaymq.common;

import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.delaymq.utils.UtilAll;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName TopicManager
 * @Description: topic管理
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
@Slf4j
public class TopicManager implements Manager, Runnable {

    private MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    protected Map<QueueTypeEnum, Map<String, TopicQueue>> topicQueueTable = new ConcurrentHashMap<>(64);

    private ConcurrentHashMap<String, List<TopicChangeListener>> topicChangeListeners = new ConcurrentHashMap<>(64);

    private ConcurrentHashMap<QueueTypeEnum, List<String>> refreshKeys = new ConcurrentHashMap<>(64);

    private TopicManager() {
    }

    private static class SingletonHolder {
        private static final TopicManager INSTANCE = new TopicManager();
    }

    public static final TopicManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public TopicQueue getTopicQueue(String topicName, QueueTypeEnum queueType, boolean refresh) {
        topicQueueTable.putIfAbsent(queueType, new ConcurrentHashMap<>(32));
        Map<String, TopicQueue> topicQueueMap = topicQueueTable.get(queueType);
        TopicQueue topicQueue = topicQueueMap.get(topicName);
        if (topicQueue == null || refresh) {
            MessageOperate messageOperate = messageOperateManager.getMessageOperate(queueType);
            String value = messageOperate.getTopicQueue(topicName);
            if (UtilAll.isNotBlank(value)) {
                TopicQueueData topicQueueData = JSONUtil.parseObject(value, TopicQueueData.class);
                topicQueue = new TopicQueue(topicName, queueType, topicQueueData);
                this.updateTopicQueueTable(topicQueue, queueType);
                return topicQueue;
            }
        }
        return topicQueue;
    }

    public void updateTopicQueueTable(final TopicQueue topicQueue, QueueTypeEnum queueType) {
        topicQueueTable.putIfAbsent(queueType, new ConcurrentHashMap<>(32));
        TopicQueue old = topicQueueTable.get(queueType).put(topicQueue.getTopicName(), topicQueue);
        if (old != null && !old.equals(topicQueue)) {
            log.info("update topic config, old:[{}] new:[{}]", old, topicQueue);
        } else if (old == null) {
            log.info("update topic config, new:[{}] ", topicQueue);
        }
        //通知update
        onTopicQueueChange(topicQueue);
    }

    @Override
    public void run() {
        refreshTopicQueue();
    }

    public void registerTopicQueue(String topicQueueName, QueueTypeEnum queueType) {
        if (refreshKeys.get(queueType) == null) {
            refreshKeys.putIfAbsent(queueType, new ArrayList<>());
        }
        if (!refreshKeys.get(queueType).contains(topicQueueName)) {
            refreshKeys.get(queueType).add(topicQueueName);
        }
    }

    public void registerTopicListener(String topicName, TopicChangeListener topicChangeListener) {
        topicChangeListeners.putIfAbsent(topicName, new ArrayList<>());
        topicChangeListeners.get(topicName).add(topicChangeListener);
    }

    /**
     * topic变更通知
     *
     * @param topicQueue
     */
    public void onTopicQueueChange(TopicQueue topicQueue) {
        List<TopicChangeListener> listeners = topicChangeListeners.get(topicQueue.getTopicName());
        if (listeners != null) {
            for (TopicChangeListener topicChangeListener : listeners) {
                topicChangeListener.onTopicQueueChange(topicQueue);
            }
        }
    }

    public void refreshTopicQueue() {
        for (QueueTypeEnum queueType : refreshKeys.keySet()) {
            for (String topicName : refreshKeys.get(queueType)) {
                refreshTopicQueue(queueType, topicName);
            }
        }
    }

    /**
     * 刷新topic 以防变更
     *
     * @param queueType
     * @param topicName
     */
    protected void refreshTopicQueue(QueueTypeEnum queueType, String topicName) {
        this.getTopicQueue(topicName, queueType, true);
        return;
    }

    /**
     * 根据topic和queueNum 获取队列名称
     *
     * @param topic
     * @param queueNum
     * @return
     */
    public static String getTopicMsgQueue(String topic, int queueNum) {
        StringBuilder sb = new StringBuilder();
        String topicMsgQueue = sb.append(MQConstant.MSG_QUEUE).append(topic).append(":").append(queueNum).toString();
        return topicMsgQueue;
    }
}
