package com.luoluo.delaymq.consumer;

import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 监听数据DTO
 *
 * @ClassName ConsumeListenerDTO
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/7/18
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ConsumeListenerDTO {

    /**
     * bean
     */
    private DelayMQConsumerListener delayMQConsumerListener;

    private DelayMQMessageListener annotation;

    private String topic;

    private String consumeGroup;

    private MQConsumer consumer;

    

}
