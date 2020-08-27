package com.luoluo.delaymq.common;

/**
 * @ClassName SendResultFuture
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/7/22
 * @Version V1.0
 **/

public interface SendResultCallback {

    void success(String msgId);

    void fail(final Throwable e);
}
