package com.luoluo.redis.config;

import com.luoluo.delaymq.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

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
