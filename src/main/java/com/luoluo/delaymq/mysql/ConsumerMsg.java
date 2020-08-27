package com.luoluo.delaymq.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
public class ConsumerMsg extends AbstractDataBO implements Serializable {

    @JsonIgnore
    private String msgId;

    private String topic;

    private String consumerGroup;

    private String consumerStatus;

    private Date consumerTime;

    private Date executeTime;

    private int retryCount;

    private Date retryNextTime;
}
