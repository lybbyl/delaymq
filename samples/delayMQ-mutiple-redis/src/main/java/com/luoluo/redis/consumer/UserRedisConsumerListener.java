package com.luoluo.redis.consumer;

import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.consumer.AbstractDelayMQConsumerListener;
import com.luoluo.delaymq.consumer.ConsumerStatus;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.redis.RedisUtils;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.redis.dataobject.UserDO;
import com.luoluo.redis.mapper.UserMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@DelayMQMessageListener(
        //必须配置监听topic
        topic = "Test"
        //必须配置消费者组
        , consumerGroup = "demo01-A-consumer-group-" + "Topic"
        //默认是Reids队列
        , queueType = QueueTypeEnum.REDIS_QUEUE
        , supportTransaction = true
)
public class UserRedisConsumerListener extends AbstractDelayMQConsumerListener<UserDO> {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    RedisTemplate redisTemplate;

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ConsumerStatus onMessage(UserDO message, String msgId) {
        //模拟消费接口耗时
        Thread.sleep(RandomBoolean.getRandom());

        UserDO updateUser = new UserDO().setId(message.getId())
                .setPassword("wobucai");
        redisTemplate.opsForValue().set("user", JSONUtil.toJSONString(updateUser));

        //模拟消费接口部分异常
        if (RandomBoolean.getRandomBool()) {
            throw new RuntimeException("RandomBoolean.getRandomBool()");
        }
        log.info("[onMessage][线程编号:{} 消息id:{} 消息内容：{}]", Thread.currentThread().getId(), msgId, JSONUtil.toJSONString(message));
        redisUtils.incr(getClass().getName());
        return ConsumerStatus.SUCCESS;
    }

}
