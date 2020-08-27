package com.luoluo.delaymq.common;

import lombok.*;

import java.io.Serializable;

/**
 * 消息
 * luoluo
 *
 * @param <T>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message<T> implements Serializable {

    private static final long serialVersionUID = 8445773977054106428L;

    /**
     * 消息id
     * 默认uuid随机生成
     * 如果外部传入，请确保唯一性
     * 如果为mysql队列 不唯一则会报错
     * 如果为redis队列 不唯一目前会覆盖消息。
     */
    private String msgId;

    /**
     * 消息执行时间
     */
    private long executeTime = System.currentTimeMillis();

    /**
     * topic
     */
    private String topicName;

    /**
     * 消息内容
     */
    private T body;

    /**
     * todo 暂不使用 后续扩展
     *
     * 消息存活时间
     * 默认存活时间=延时时间+默认存活时间,
     * 配置此项存活时间=延时时间+默认存活时间+ttl秒,即不配置则消息存活时间为默认存活时间
     * 仅适用于redis
     */
    private int ttl;

}
