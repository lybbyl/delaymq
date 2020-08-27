package com.luoluo.delaymq.mysql;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassName ConsumerMsgData
 * @Description: 消费数据记录
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordConsumeTime extends AbstractDataBO implements Serializable {

    private String consumerGroup;

    private String topicName;

    private int queueNum;

    private Long consumeTime;

}
