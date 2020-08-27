package com.luoluo.delaymq.common;

import com.luoluo.delaymq.constant.QueueTypeEnum;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ClassName TopicQueue
 * @Description: topic数据结构
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicQueue implements Serializable {

    private String topicName;

    private QueueTypeEnum queueType;

    private TopicQueueData topicQueueData;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TopicQueue that = (TopicQueue) o;
        return Objects.equals(topicName, that.topicName) &&
                queueType == that.queueType &&
                Objects.equals(topicQueueData, that.topicQueueData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, queueType, topicQueueData);
    }
}
