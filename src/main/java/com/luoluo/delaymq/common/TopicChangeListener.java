package com.luoluo.delaymq.common;

/**
 * @Date: 2020/8/3 16:41
 * @Author: luoluo
 * @Description:  topic 变更通知
 */
public interface TopicChangeListener {

    void onTopicQueueChange(TopicQueue topicQueue);
}
