package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.common.SpringContext;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.lock.RedisLock;
import com.luoluo.delaymq.producer.RedisMQProducer;
import com.luoluo.delaymq.redis.RedisMessageOperate;
import com.luoluo.delaymq.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2020/07/20
 * @description Redis延时队列自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DelayMQProperties.class)
public class RedisQueueAutoConfiguration {

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    RedissonClient redissonClient;

    private MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    @Bean
    @ConditionalOnMissingBean(SpringContext.class)
    public SpringContext springContext() {
        return new SpringContext();
    }

    @Bean
    @ConditionalOnMissingBean(RedisUtils.class)
    public RedisUtils redisUtils() {
        RedisUtils redisUtils = new RedisUtils();
        redisUtils.setRedisTemplate(template);
        return redisUtils;
    }

    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock(RedisUtils redisUtils) {
        return new RedisLock(redisUtils, redissonClient);
    }

    @Bean
    @ConditionalOnBean(RedisUtils.class)
    @ConditionalOnMissingBean(RedisMessageOperate.class)
    public RedisMessageOperate redisMessageOperate(RedisUtils redisUtils,DelayMQProperties delayMQProperties) {
        RedisMessageOperate redisMessageOperate = new RedisMessageOperate();
        redisMessageOperate.setRedisUtils(redisUtils);
        redisMessageOperate.setMsgSurviveTime(delayMQProperties.getProducer().getMsgSurviveTime());
        messageOperateManager.addMessageOperate(QueueTypeEnum.REDIS_QUEUE, redisMessageOperate);
        return redisMessageOperate;
    }

    @Bean
    @ConditionalOnBean(RedisMessageOperate.class)
    @ConditionalOnMissingBean(RedisMQProducer.class)
    public RedisMQProducer redisMQProducer(DelayMQProperties delayMQProperties, DistributedLock distributedLock,RedisMessageOperate redisMessageOperate) {
        RedisMQProducer redisMQProducer = new RedisMQProducer(delayMQProperties, redisMessageOperate, distributedLock);
        return redisMQProducer;
    }

    @Bean
    @ConditionalOnMissingBean(DefaultListenerContainerConfiguration.class)
    public DefaultListenerContainerConfiguration createConsume(StandardEnvironment environment, DelayMQProperties delayMQProperties, DistributedLock distributedLock) {
        return new DefaultListenerContainerConfiguration(environment, delayMQProperties, distributedLock);
    }

}
