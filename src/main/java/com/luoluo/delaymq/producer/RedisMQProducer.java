package com.luoluo.delaymq.producer;

import com.luoluo.delaymq.common.MessageOperate;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.lock.DistributedLock;

/**
 * @ClassName RedisMQProducer
 * @Description: 生产者类型拆分
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
public class RedisMQProducer extends DefaultMQProducer {

    public RedisMQProducer(DelayMQProperties delayMQProperties, MessageOperate messageOperate, DistributedLock distributedLock) {
        super(delayMQProperties, messageOperate, distributedLock);
    }

}
