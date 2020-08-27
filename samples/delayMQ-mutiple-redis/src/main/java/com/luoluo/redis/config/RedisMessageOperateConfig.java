package com.luoluo.redis.config;

import com.luoluo.delaymq.common.MessageOperateManager;
import com.luoluo.delaymq.constant.QueueTypeEnum;
import com.luoluo.delaymq.mysql.MySQLMessageOperate;
import com.luoluo.delaymq.redis.RedisMessageOperate;
import com.luoluo.delaymq.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @ClassName MySqlMessageOperate
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/8/26
 * @Version V1.0
 **/
@Configuration
public class RedisMessageOperateConfig {

    @Bean
    @ConditionalOnMissingBean(RedisUtils.class)
    public RedisUtils redisUtils(@Qualifier("delaymqRedisTemplate") RedisTemplate redisTemplate) {
        RedisUtils redisUtils = new RedisUtils();
        redisUtils.setRedisTemplate(redisTemplate);
        return redisUtils;
    }

}
