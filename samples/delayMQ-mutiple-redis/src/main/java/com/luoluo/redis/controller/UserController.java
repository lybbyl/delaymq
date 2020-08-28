package com.luoluo.redis.controller;

import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.producer.DelayMQTransactionState;
import com.luoluo.delaymq.producer.RedisMQProducer;
import com.luoluo.delaymq.producer.annotation.AbstractDelayMQTransactionListener;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import com.luoluo.delaymq.utils.JSONUtil;
import com.luoluo.redis.dataobject.UserDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @ClassName UserController
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/8/26
 * @Version V1.0
 **/
@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisMQProducer redisMQProducer;

    @GetMapping(value = "/testInsertUser")
    @Transactional(rollbackFor = Throwable.class)
    public void testInsertUser() {
        UserDO user = new UserDO().setUsername(UUID.randomUUID().toString())
                .setPassword("nicai").setCreateTime(new Date());

        redisTemplate.opsForValue().set("user", JSONUtil.toJSONString(user));

        // 创建 Demo01Message 消息
        String msgId = UUID.randomUUID().toString();
        msgId = redisMQProducer.sendMessage(user, "Test", System.currentTimeMillis(), msgId);

    }

    @DelayMQTransactionListener(topicName = "Test", queueType = QueueTypeEnum.MYSQL_QUEUE)
    public class TransactionListenerImpl extends AbstractDelayMQTransactionListener<UserDO> {

        @Autowired
        private RedisTemplate redisTemplate;

        /**
         * 第一次先有本地确认事务是否需要提交
         *
         * @param demo01Message
         * @return
         */
        @Override
        public DelayMQTransactionState executeLocalTransaction(UserDO demo01Message, String msgId) {
            return DelayMQTransactionState.UNKNOWN;
        }

        /**
         * 回调确认是否需要提交
         *
         * @param demo01Message
         * @return
         */
        @Override
        public DelayMQTransactionState checkLocalTransaction(UserDO demo01Message, String msgId) {
            if (redisTemplate.opsForValue().get(demo01Message.getId()) != null) {
                return DelayMQTransactionState.COMMIT;
            }
            return DelayMQTransactionState.ROLLBACK;
        }

    }
}
