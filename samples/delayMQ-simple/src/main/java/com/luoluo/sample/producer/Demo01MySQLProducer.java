package com.luoluo.sample.producer;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.common.SendResultCallback;
import com.luoluo.delaymq.common.SendResultFuture;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.producer.DelayMQTransactionState;
import com.luoluo.delaymq.producer.MySQLProducer;
import com.luoluo.delaymq.producer.annotation.AbstractDelayMQTransactionListener;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.sample.message.Demo01Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/test")
public class Demo01MySQLProducer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 引入MYSQL队列发送方式
     */
    @Autowired
    private MySQLProducer mySQLProducer;

    /**
     * 同步发送
     */
    @GetMapping(value = "/sendMySQLMessage")
    @Transactional(rollbackFor = Throwable.class)
    public void syncSend() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(UUID.randomUUID().toString());
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date(currentTimeMillis));
        System.out.println(demo01Message.getId() + format);
        //发送消息demo01Message, Topic：Test,默认立即消费
        String msgId = mySQLProducer.sendMessage(demo01Message, "Test");

//        //发送消息demo01Message, Topic：Test, 五秒后消费
//        mySQLProducer.sendMessage(demo01Message, "Test", currentTimeMillis + 5000L);
//
//        //发送消息demo01Message, Topic：Test, 当前消费，设置消息id
//        //如果不设置 则默认使用uuid作为消息主键 请确认保证消息id唯一
//        //如果消息id重复 mysql队列插入报错 redis队列会覆盖消息
//        String msgId = mySQLProducer.sendMessage(demo01Message, "Test", currentTimeMillis, demo01Message.getId());
        System.out.println("唯一消息id" + msgId);
    }

    @GetMapping(value = "/sendMySQLMessage2")
    public void syncSend2() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        //同步发送第二种方式  构造好Message类
        String msgId = mySQLProducer.sendMessage(message);
        logger.info("消息id：{}，成功发送。", msgId);
    }

    @GetMapping(value = "/syncSendMySQLMessage")
    public void syncSendMySQLMessage() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息
        SendResultFuture sendResultFuture = mySQLProducer.syncSendMessage(demo01Message, "Test", System.currentTimeMillis(), UUID.randomUUID().toString());
        ;
        // 同步发送消息 超时0.5s
        SendResultFuture sendResultFuture1 = mySQLProducer.syncSendMessage(demo01Message, "Test", System.currentTimeMillis(), UUID.randomUUID().toString(), 500);
        ;

        System.out.println(sendResultFuture.isSuccess());
    }

    @GetMapping(value = "/syncSendMySQLMessage2")
    public void syncSendMySQLMessage2() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息设置超时 单位ms
        demo01Message.setId(System.currentTimeMillis() + "timeout_sendMessage");
        SendResultFuture sendResultFuture = mySQLProducer.syncSendMessage(message, 100);
        System.out.println(sendResultFuture.isSuccess());
        if (!sendResultFuture.isSuccess()) {
            sendResultFuture.getThrowable().printStackTrace();
        }
    }

    @GetMapping(value = "/asyncSendMySQLMessageFuture")
    public void asyncSendMessageFuture() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "asyncSendMessageFuture");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息
        mySQLProducer.asyncSendMessageFuture(message);

    }

    /**
     * 异步发送回调
     * 某些情况下 我们需要异步发送 并适时得到回调结果
     * 如下示例
     */
    @GetMapping(value = "/asyncSendMySQLMessageCallback")
    public void asyncSendMessageCallback() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "asyncSendMessageCallback");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        // 同步发送消息
        mySQLProducer.asyncSendMessageCallback(message, new SendResultCallback() {
            @Override
            public void success(String msgId) {
                System.out.println(msgId);
                System.out.println("success");
            }

            @Override
            public void fail(Throwable e) {
                System.out.println("fail");
                e.printStackTrace();
            }
        });
    }

    @GetMapping(value = "/hashSendMySQLMessage")
    public void hashSendMessage() {
        String hashKey = "orderId";
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage1");
        //同步hash发送第一种方式 执行时间为当前 则将立即被消费
        long timestamp = System.currentTimeMillis();
        mySQLProducer.hashSendMessage(demo01Message, "Test", hashKey, timestamp);

        demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage2");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        //注意一定要符合执行时间顺序 才可保证有序
        message.setExecuteTime(timestamp + 1);
        //同步发送第二种方式  构造好Message类
        mySQLProducer.hashSendMessage(message, hashKey);
    }

    @GetMapping(value = "/testWhileMySQLProducer")
    public void testWhileProducer() {
        long startTimestamp = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTimestamp + 1000L) {
            asyncSendMessageFuture();
        }
    }

    @GetMapping(value = "/sendMySQLTransactionMessage")
    public void sendTransactionMessage() {
        // 创建 Demo01Message 消息
        int transactionValue = atomicInteger.getAndIncrement();
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMySQLTransactionMessage");
        demo01Message.setTransactionValue(transactionValue);
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        // 同步发送消息
        mySQLProducer.sendTransactionMessage(message);

    }

    @DelayMQTransactionListener(topicName = "Test", queueType = QueueTypeEnum.MYSQL_QUEUE)
    public class TransactionListenerImpl extends AbstractDelayMQTransactionListener<Demo01Message> {

        /**
         * 第一次先有本地确认事务是否需要提交
         *
         * @param demo01Message
         * @return
         */
        @Override
        public DelayMQTransactionState executeLocalTransaction(Demo01Message demo01Message, String msgId) {
            logger.info("[executeLocalTransaction][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
            if (demo01Message.getTransactionValue() % 5 == 0) {
                return DelayMQTransactionState.COMMIT;
            }
            if (demo01Message.getTransactionValue() % 5 == 1) {
                return DelayMQTransactionState.ROLLBACK;
            }
            return DelayMQTransactionState.UNKNOWN;
        }

        /**
         * 回调确认是否需要提交
         *
         * @param demo01Message
         * @return
         */
        @Override
        public DelayMQTransactionState checkLocalTransaction(Demo01Message demo01Message, String msgId) {
            logger.info("[checkLocalTransaction][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
            if (demo01Message.getTransactionValue() % 5 == 2) {
                return DelayMQTransactionState.COMMIT;
            }
            if (demo01Message.getTransactionValue() % 5 == 3) {
                return DelayMQTransactionState.ROLLBACK;
            }
            return DelayMQTransactionState.UNKNOWN;
        }

        /**
         * 扩展接口
         * 事务消息提交后的扩展（第一次本地确认 和回调确认都会触发）
         *
         * @param demo01Message
         */
        @Override
        public void afterCommitTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterCommitTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);

        }

        /**
         * 扩展接口
         * 事务消息提交后的扩展（第一次本地确认 和回调确认都会触发）
         *
         * @param demo01Message
         */
        @Override
        public void afterRollbackTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterRollbackTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);

        }

        /**
         * 扩展接口
         * 超过次数回滚事务消息（超过重试次数触发）
         *
         * @param demo01Message
         */
        @Override
        public void afterOverRetryTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterOverRetryTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
        }

        /**
         * 扩展接口
         * 事务消息待确认的扩展（异常+不确定 触发）
         *
         * @param demo01Message
         */
        @Override
        public void afterDelayTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterDelayTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
        }
    }

}
