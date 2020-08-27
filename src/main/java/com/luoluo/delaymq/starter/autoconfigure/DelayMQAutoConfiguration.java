package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.lock.DistributedLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

/**
 * @ClassName DelayMQAutoConfiguration
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@Configuration
@Import({RedisQueueAutoConfiguration.class, MySQLQueueAutoConfiguration.class, DelayMQTransactionAnnotationProcessor.class})
public class DelayMQAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DefaultListenerContainerConfiguration.class)
    public DefaultListenerContainerConfiguration createConsume(StandardEnvironment environment, DelayMQProperties delayMQProperties, DistributedLock distributedLock) {
        return new DefaultListenerContainerConfiguration(environment, delayMQProperties, distributedLock);
    }
}
