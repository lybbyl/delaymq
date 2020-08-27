package com.luoluo.delaymq.common;

import com.luoluo.delaymq.service.rebalance.RebalanceStrategyEnum;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName TopicQueue
 * @Description: TopicQueue数据结构
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
public class TopicQueueData implements Serializable {

    /**
     * queueName=topicName+Queue+':'+num
     */
    private List<String> queueNames;

    /**
     * 默认自增取余入队
     */
    private RebalanceStrategyEnum rebalanceStrategyEnum = RebalanceStrategyEnum.ROUND;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TopicQueueData that = (TopicQueueData) o;
        return compareTopicQueueData(this.queueNames, that.getQueueNames()) &&
                rebalanceStrategyEnum == that.rebalanceStrategyEnum;
    }

    private boolean compareTopicQueueData(List<String> queueNames, List<String> queueNames1) {
        if (queueNames == null && queueNames1 == null) {
            return true;
        }
        if (queueNames == null || queueNames1 == null) {
            return false;
        }
        if (queueNames.size() != queueNames1.size()) {
            return false;
        }
        return queueNames.containsAll(queueNames1) && queueNames1.containsAll(queueNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueNames, rebalanceStrategyEnum);
    }
}
