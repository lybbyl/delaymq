package com.luoluo.mybatis.config;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.mysql.MySQLMessageOperate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @ClassName MySqlMessageOperate
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/8/26
 * @Version V1.0
 **/
@Configuration
public class MySQLMessageOperateConfig {

    private MessageOperateManager messageOperateManager = MessageOperateManager.getInstance();

    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnProperty(name = "com.luoluo.delaymq.allow-mysql")
    @ConditionalOnMissingBean(MySQLMessageOperate.class)
    public MySQLMessageOperate mySQLMessageOperate(@Qualifier("businessJdbcTemplate") JdbcTemplate jdbcTemplate) {
        MySQLMessageOperate mysqlMessageOperate = new MySQLMessageOperate();
        mysqlMessageOperate.setJdbcTemplate(jdbcTemplate);
        messageOperateManager.addMessageOperate(QueueTypeEnum.MYSQL_QUEUE, mysqlMessageOperate);
        return mysqlMessageOperate;
    }
}
