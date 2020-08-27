package com.luoluo.delaymq.constant;

/**
 * @Date: 2020/07/20
 * @Author: luoluo.
 */
public class MQConstant {

    /**
     * MSG_STORE:延时消息池前缀
     */
    public static final String MSG_STORE = "MESSAGE:STORE:";

    /**
     * MSG_TRANSACTION_STORE:事务消息池前缀
     */
    public static final String MSG_TRANSACTION_STORE = "MESSAGE:TRANSACTION:STORE:";

    /**
     * MESSAGE_TOPIC_TABLE:Topic存储池前缀
     */
    public static final String MESSAGE_TOPIC_TABLE = "MESSAGE:TOPIC:TABLE:";

    /**
     * MESSAGE_TOPIC_TABLE:Topic存储池前缀
     */
    public static final String MESSAGE_TOPIC_CONSUME = "MESSAGE:TOPIC:CONSUME:";

    /**
     * Message:Transaction:RETRY:消息池前缀
     */
    public static final String MSG_TRANSACTION_RETRY = "MESSAGE:TRANSACTION:RETRY:";

    /**
     * MSG_POOL:消息池前缀
     */
    public static final String MSG_DELAY = ":DELAY";

    /**
     * MESSAGE:LOCK:POOL:消息池锁前缀
     */
    public static final String MSG_LOCK_POOL = "MESSAGE:LOCK:POOL:";

    /**
     * MESSAGE:CONSUME:记录消费消息池前缀
     */
    public static final String MSG_CONSUME = "MESSAGE:CONSUME:";

    /**
     * MESSAGE:QUEUE:消息队列前缀
     */
    public static final String MSG_QUEUE = "MESSAGE:QUEUE:";

    /**
     * 默认事务全局名称
     */
    public static final String TRANSACTION_GLOBAL_NAME = "MESSAGE:QUEUE:TRANSACTION";

    /**
     * 0
     */
    public static final int CONSUME_ZERO_SIZE = 0;

    /**
     * 默认拉取消息数量
     */
    public static final int DEFAULT_CONSUME_PULL_MESSAGE_SIZE = 2000;

    /**
     * 默认消费超时时间
     */
    public static final int DEFAULT_CONSUME_TIME_OUT = 15;

    /**
     * 默认消费重试次数
     */
    public static final int DEFAULT_CONSUME_RETRY_OUT = 8;

    /**
     * 默认消费下次重试时间
     */
    public static final int DEFAULT_CONSUME_RETRY_DELAY_TIME = 15;

    /**
     * 默认消费回溯时间
     */
    public static final int DEFAULT_CONSUME_BACK_TRACK_TIME = 3;
}
