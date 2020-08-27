package com.luoluo.delaymq.constant;

import com.luoluo.delaymq.common.MessageOperate;
import com.luoluo.delaymq.mysql.MySQLMessageOperate;
import com.luoluo.delaymq.redis.RedisMessageOperate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @ClassName QueueTypeEnum
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@NoArgsConstructor
public enum QueueTypeEnum {

    /**
     * 基于redis实现的队列
     */
    REDIS_QUEUE("基于redis实现的队列", RedisMessageOperate.class),
    /**
     * 基于mysql实现的队列
     */
    MYSQL_QUEUE("基于mysql实现的队列", MySQLMessageOperate.class);

    QueueTypeEnum(String description, Class<? extends MessageOperate> messageOperateClass) {
        this.description = description;
        this.messageOperateClass = messageOperateClass;
    }

    @Getter
    @Setter
    String description;

    @Getter
    @Setter
    Class<? extends MessageOperate> messageOperateClass;
}
