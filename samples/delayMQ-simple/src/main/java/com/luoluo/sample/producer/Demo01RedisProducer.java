package com.luoluo.sample.producer;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.common.SendResultCallback;
import com.luoluo.delaymq.common.SendResultFuture;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.producer.DelayMQTransactionState;
import com.luoluo.delaymq.producer.RedisMQProducer;
import com.luoluo.delaymq.producer.annotation.AbstractDelayMQTransactionListener;
import com.luoluo.delaymq.producer.annotation.DelayMQTransactionListener;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.sample.message.Demo01Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/test")
public class Demo01RedisProducer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Autowired
    private RedisMQProducer redisMQProducer;

    @GetMapping(value = "/sendRedisMessage")
    public void syncSend() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(UUID.randomUUID().toString());
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date(currentTimeMillis));
        System.out.println(demo01Message.getId() + format);
        //发送消息demo01Message, Topic：Test,默认立即消费
        String msgId = redisMQProducer.sendMessage(demo01Message, "Test");

//        //发送消息demo01Message, Topic：Test, 五秒后消费
//        redisMQProducer.sendMessage(demo01Message, "Test", currentTimeMillis + 5000L);
//
//        //发送消息demo01Message, Topic：Test, 当前消费，设置消息id
//        //如果不设置 则默认使用uuid作为消息主键 请确认保证消息id唯一
//        //如果消息id重复 mysql队列插入报错 redis队列会覆盖消息
//        String msgId = redisMQProducer.sendMessage(demo01Message, "Test", currentTimeMillis, demo01Message.getId());
        System.out.println("唯一消息id" + msgId);
    }

    @GetMapping(value = "/sendRedisMessage2")
    public void syncSend2() {

        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage");

        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        redisMQProducer.sendMessage(message);
    }

    @GetMapping(value = "/syncSendRedisMessage")
    public void syncSendRedisMessage() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息
        SendResultFuture sendResultFuture = redisMQProducer.syncSendMessage(message);
        System.out.println(sendResultFuture.isSuccess());

        demo01Message.setId(System.currentTimeMillis() + "timeout_sendMessage");
        sendResultFuture = redisMQProducer.syncSendMessage(message, 100);
        System.out.println(sendResultFuture.isSuccess());
        if (!sendResultFuture.isSuccess()) {
            sendResultFuture.getThrowable().printStackTrace();
        }
    }

    @GetMapping(value = "/syncSendRedisMessage2")
    public void syncSendRedisMessage2() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息设置超时 单位ms
        demo01Message.setId(System.currentTimeMillis() + "timeout_sendMessage");
        SendResultFuture sendResultFuture = redisMQProducer.syncSendMessage(message, 100);
        System.out.println(sendResultFuture.isSuccess());
        if (!sendResultFuture.isSuccess()) {
            sendResultFuture.getThrowable().printStackTrace();
        }
    }

    @GetMapping(value = "/asyncSendRedisMessageFuture")
    public void asyncSendMessageFuture() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "asyncSendMessageFuture");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis() + DateNumUtil.FIVE_SECOND);
        // 同步发送消息
        redisMQProducer.asyncSendMessageFuture(message);
    }

    /**
     * 异步发送回调
     * 某些情况下 我们需要异步发送 并适时得到回调结果
     * 如下示例
     */
    @GetMapping(value = "/asyncSendRedisMessageCallback")
    public void asyncSendMessageCallback() {
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "asyncSendMessageCallback");
        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        // 同步发送消息
        redisMQProducer.asyncSendMessageCallback(message, new SendResultCallback() {

            @Override
            public void success(String msgId) {
                System.out.println(msgId);
            }

            @Override
            public void fail(Throwable e) {
                System.out.println("fail");
                e.printStackTrace();
            }
        });
    }

    @GetMapping(value = "/hashSendRedisMessage")
    public void hashSendMessage() {
        String hashKey = "orderId";
        // 创建 Demo01Message 消息
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendMessage1");
        //同步hash发送第一种方式 执行时间为当前 则将立即被消费
        redisMQProducer.hashSendMessage(demo01Message, "Test", hashKey);

    }

    @GetMapping(value = "/testWhileRedisProducer")
    public void testWhileProducer() {
        long startTimestamp = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTimestamp + 1000L) {
            asyncSendMessageFuture();
        }
    }

    @GetMapping(value = "/sendRedisTransactionMessage")
    public void sendTransactionMessage() {
        // 创建 Demo01Message 消息
        int transactionValue = atomicInteger.getAndIncrement();
        Demo01Message demo01Message = new Demo01Message();
        demo01Message.setId(System.currentTimeMillis() + "sendRedisTransactionMessage");
        demo01Message.setTransactionValue(transactionValue);

        Message<Demo01Message> message = new Message<>();
        message.setBody(demo01Message);
        message.setTopicName("Test");
        message.setExecuteTime(System.currentTimeMillis());
        // 同步发送消息
        redisMQProducer.sendTransactionMessage(message);
    }

    /**
     * 确保Topic和队列类型能够对应一致
     */
    @DelayMQTransactionListener(topicName = "Test", queueType = QueueTypeEnum.REDIS_QUEUE /**默认事务队列类型即REDIS*/)
    public class TransactionListenerImpl extends AbstractDelayMQTransactionListener<Demo01Message> {

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

        @Override
        public void afterCommitTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterCommitTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);

        }

        @Override
        public void afterRollbackTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterRollbackTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);

        }

        @Override
        public void afterOverRetryTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterOverRetryTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
        }

        @Override
        public void afterDelayTransactionMessage(Demo01Message demo01Message, String msgId) {
            logger.info("[afterDelayTransactionMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo01Message);
        }
    }
}
