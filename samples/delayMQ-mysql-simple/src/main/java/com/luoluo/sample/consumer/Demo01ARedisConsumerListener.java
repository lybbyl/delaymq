package com.luoluo.sample.consumer;

import com.luoluo.delaymq.consumer.AbstractDelayMQConsumerListener;
import com.luoluo.delaymq.consumer.ConsumerStatus;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.redis.RedisUtils;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.sample.message.Demo01Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@DelayMQMessageListener(
        //监听的topic
        topic = "Test"
        //消费者组
        //默认消费模式为集群模式 ，一条消息只能被一个消费者组中的一个消费者消费
        , consumerGroup = "demo01-A-consumer-group-" + "Topic"
)
@Controller
public class Demo01ARedisConsumerListener extends AbstractDelayMQConsumerListener<Demo01Message> {


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
