package com.luoluo.delaymq.common;

import com.luoluo.delaymq.constant.QueueTypeEnum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName MessageOperateManager
 * @Description: 操作类管理
 * @Author luoluo
 * @Date 2020/8/17
 * @Version V1.0
 **/
public class MessageOperateManager {

    protected ConcurrentHashMap<QueueTypeEnum, MessageOperate> messageOperateMap = new ConcurrentHashMap<>(4);

    private MessageOperateManager() {
    }

    private static class SingletonHolder {
        private static final MessageOperateManager INSTANCE = new MessageOperateManager();
    }

    public static final MessageOperateManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addMessageOperate(QueueTypeEnum queueType, MessageOperate messageOperate) {
        messageOperateMap.put(queueType, messageOperate);
    }

    public MessageOperate getMessageOperate(QueueTypeEnum queueType) {
        MessageOperate messageOperate = messageOperateMap.get(queueType);
        if (messageOperate != null) {
            return messageOperate;
        }
        return null;
    }

}
