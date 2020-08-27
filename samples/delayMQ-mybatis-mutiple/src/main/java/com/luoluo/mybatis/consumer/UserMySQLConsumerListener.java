package com.luoluo.mybatis.consumer;

import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.consumer.AbstractDelayMQConsumerListener;
import com.luoluo.delaymq.consumer.ConsumerStatus;
import com.luoluo.delaymq.consumer.annotation.DelayMQMessageListener;
import com.luoluo.delaymq.redis.RedisUtils;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.mybatis.dataobject.UserDO;
import com.luoluo.mybatis.mapper.UserMapper;
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
        //必须配置监听topic
        topic = "Test"
        //必须配置消费者组
        , consumerGroup = "demo01-A-consumer-group-" + "Topic"
        //默认是Reids队列，更改为MYSQL_QUEUE即可
        , queueType = QueueTypeEnum.MYSQL_QUEUE
        , supportTransaction = true
)
public class UserMySQLConsumerListener extends AbstractDelayMQConsumerListener<UserDO> {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    UserMapper userMapper;

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ConsumerStatus onMessage(UserDO message, String msgId) {
        //模拟消费接口耗时
        Thread.sleep(RandomBoolean.getRandom());

        UserDO updateUser = new UserDO().setId(message.getId())
                .setPassword("wobucai");
        userMapper.updateById(updateUser);

        //模拟消费接口部分异常
        if (RandomBoolean.getRandomBool()) {
            throw new RuntimeException("RandomBoolean.getRandomBool()");
        }
        log.info("[onMessage][线程编号:{} 消息id:{} 消息内容：{}]", Thread.currentThread().getId(), msgId, JSONUtil.toJSONString(message));
        redisUtils.incr(getClass().getName());
        return ConsumerStatus.SUCCESS;
    }

}
