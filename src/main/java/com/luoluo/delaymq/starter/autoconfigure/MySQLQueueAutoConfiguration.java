package com.luoluo.delaymq.starter.autoconfigure;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.lock.DistributedLock;
import com.luoluo.delaymq.mysql.MySQLMessageOperate;
import com.luoluo.delaymq.producer.MySQLProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2019/5/29
 * @description MySQL 队列自动配置 前置条件 引入spring-boot-jdbc
 */
@Slf4j
@Configuration
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(DelayMQProperties.class)
public class MySQLQueueAutoConfiguration {

    private MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnProperty(name = "com.luoluo.delaymq.allow-mysql")
    @ConditionalOnMissingBean(MySQLMessageOperate.class)
    public MySQLMessageOperate mySQLMessageOperate() {
        MySQLMessageOperate mysqlMessageOperate = new MySQLMessageOperate();
        messageOperateManager.addMessageOperate(QueueTypeEnum.MYSQL_QUEUE, mysqlMessageOperate);
        return mysqlMessageOperate;
    }

    @Bean
    @ConditionalOnBean(MySQLMessageOperate.class)
    @ConditionalOnMissingBean(MySQLProducer.class)
    public MySQLProducer mySQLProducer(DelayMQProperties delayMQProperties, DistributedLock distributedLock, MySQLMessageOperate mysqlMessageOperate) {
        MySQLProducer mySQLProducer = new MySQLProducer(delayMQProperties, mysqlMessageOperate, distributedLock);
        return mySQLProducer;
    }

}
