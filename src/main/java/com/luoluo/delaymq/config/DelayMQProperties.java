package com.luoluo.delaymq.config;

import com.luoluo.delaymq.constant.ConsumeGroupType;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 *
 * @Date: 2020/07/20
 * @Author: luoluo
 */
@ConfigurationProperties(prefix = DelayMQProperties.PREFIX)
public class DelayMQProperties implements Config {

    /**
     * 配置类前缀
     */
    public static final String PREFIX = "com.luoluo.delaymq";

    /**
     * 生产者配置
     */
    @Setter
    @Getter
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    @Setter
    @Getter
    private Consumer consumer = new Consumer();

    /**
     * 是否允许开启mysql作为队列
     */
    @Setter
    @Getter
    private boolean allowMysql = false;

    @Setter
    @Getter
    public static class Producer {

        /**
         * 默认可以自动创建topic
         */
        private boolean autoCreatTopic = true;

        /**
         * 默认1个topic对应4个写队列
         */
        private int writeTopicQueueNum = 4;

        /**
         * 默认存活时间为24小时
         */
        private int msgSurviveTime = 60 * 60 * 24;

    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Setter
    @Getter
    public static final class Consumer {

        /**
         * 可配置关闭消费者 参考rocketMQ
         */
        private Map<String, Map<String, Boolean>> listeners = new HashMap<>();

        /**
         * 默认REDIS队列
         */
        private QueueTypeEnum queueType = QueueTypeEnum.REDIS_QUEUE;

        /**
         * 消费的两种模式
         */
        private ConsumeGroupType consumeGroupType = ConsumeGroupType.MERGE;

        /**
         * 监听bean消费线程数
         */
        private int consumeThread = 4;

        /**
         * 监听bean最大消费线程数
         */
        private int consumeThreadMax = 4;

        /**
         * 默认拉取message最大大小
         */
        private int pullMessageSize = 2000;

        /**
         * 默认重试次数
         */
        private int retryCount = 8;

        /**
         * 下次重试间隔时间
         */
        private int retryDelayTime = 15;

        /**
         * 消息回溯时间
         */
        private int backTrackTime = 15;

        /**
         * 消费超时 todo
         */
        private int consumeTimeout = 5;

        /**
         * 一个队列对应一个拉取线程
         */
        private int mergeConsumeThread = 16;

        /**
         * 服务启动往回扫描的队列时间 单位s 配置-1为
         */
        private int reverseTime = -1;

        /**
         * 默认不支持事务消费
         */
        private boolean supportTransaction = false;
    }

}
