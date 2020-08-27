package com.luoluo.delaymq.common;

import lombok.*;

/**
 * @ClassName SendResultFuture
 * @Description: 异步future
 * @Author luoluo
 * @Date 2020/7/22
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SendResultFuture {

    private boolean success;

    private String msgId;

    private Throwable throwable;

    public SendResultFuture(boolean success, String msgId) {
        this.success = success;
        this.msgId = msgId;
    }

    public SendResultFuture(boolean success, Throwable throwable) {
        this.success = success;
        this.throwable = throwable;
    }
}
