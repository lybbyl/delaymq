package com.luoluo.delaymq.producer;

/**
 * @ClassName DelayMQTransactionState
 * @Description: 事务状态枚举
 * @Author luoluo
 * @Date 2020/8/6
 * @Version V1.0
 **/
public enum DelayMQTransactionState {

    /**
     * 提交
     */
    COMMIT,
    /**
     * 回滚
     */
    ROLLBACK,
    /**
     * 待确认
     */
    UNKNOWN
}
