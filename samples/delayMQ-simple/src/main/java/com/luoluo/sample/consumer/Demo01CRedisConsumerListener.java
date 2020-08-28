package com.luoluo.sample.consumer;

import com.luoluo.delaymq.consumer.AbstractDelayMQConsumerListener;
import com.luoluo.delaymq.consumer.ConsumerStatus;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.redis.RedisUtils;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.sample.message.Demo01Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@DelayMQMessageListener(
        topic = "Test"
        , consumerGroup = "demo01-C-consumer-group-" + "Topic"
        , consumeThread = 4
        , consumeThreadMax = 8
        , retryDelayTime = 1
)
public class Demo01CRedisConsumerListener extends AbstractDelayMQConsumerListener<Demo01Message> {

    @Autowired
    RedisUtils redisUtils;

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ConsumerStatus onMessage(Demo01Message message, String msgId) {
        //模拟消费接口耗时
        Thread.sleep(RandomBoolean.getRandom());
        //模拟消费接口部分异常
        if (RandomBoolean.getRandomBool()) {
            throw new RuntimeException("RandomBoolean.getRandomBool()");
        }
        log.info("[onMessage][线程编号:{} 消息id:{} 消息内容：{}]", Thread.currentThread().getId(), msgId, JSONUtil.toJSONString(message));
        redisUtils.incr(getClass().getName());
        return ConsumerStatus.SUCCESS;
    }

}
