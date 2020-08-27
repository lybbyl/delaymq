package com.luoluo.delaymq.common;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @ClassName ConsumerMsgData
 * @Description: 记录消息消费情况
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
public class ConsumerMsgData implements Serializable {

    private String id;

    private String consumerGroup;

    private String consumerStatus;

    private Date createdTime;

    private Date consumerTime;

    private Date executeTime;

    private Integer retryCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsumerMsgData that = (ConsumerMsgData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
