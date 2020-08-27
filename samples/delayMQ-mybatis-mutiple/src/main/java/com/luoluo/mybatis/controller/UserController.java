package com.luoluo.mybatis.controller;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.producer.DelayMQTransactionState;
import com.luoluo.delaymq.producer.MySQLProducer;
import com.luoluo.delaymq.producer.RedisMQProducer;
import com.luoluo.delaymq.producer.annotation.AbstractDelayMQTransactionListener;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import com.luoluo.mybatis.dataobject.UserDO;
import com.luoluo.mybatis.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
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
    private UserMapper userMapper;

    @Autowired
    private RedisMQProducer redisMQProducer;

    @Autowired
    private MySQLProducer mySQLProducer;

    @GetMapping(value = "/testInsertUser")
    @Transactional(rollbackFor = Throwable.class)
    public void testInsertUser() {
        UserDO user = new UserDO().setUsername(UUID.randomUUID().toString())
                .setPassword("nicai").setCreateTime(new Date());
        userMapper.insert(user);

        // 创建 Demo01Message 消息
        String msgId = UUID.randomUUID().toString();
        msgId = mySQLProducer.sendMessage(user, "Test", System.currentTimeMillis(), msgId);

    }

    @GetMapping(value = "/testUpdateUserById")
    @Transactional(rollbackFor = Throwable.class)
    public void testUpdateUserById() {
        UserDO updateUser = new UserDO().setId(1)
                .setPassword("wobucai");
        userMapper.updateById(updateUser);
    }

    @DelayMQTransactionListener(topicName = "Test", queueType = QueueTypeEnum.MYSQL_QUEUE)
    public class TransactionListenerImpl extends AbstractDelayMQTransactionListener<UserDO> {

        @Autowired
        private UserMapper userMapper;

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
            if (userMapper.selectById(demo01Message.getId()) != null) {
                return DelayMQTransactionState.COMMIT;
            }
            return DelayMQTransactionState.ROLLBACK;
        }

    }
}
