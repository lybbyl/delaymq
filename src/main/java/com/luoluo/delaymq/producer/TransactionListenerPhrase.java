package com.luoluo.delaymq.producer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * @ClassName RedisMQProducer
 * @Description: 事务bean解析
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class TransactionListenerPhrase {

    private DelayMQLocalTransactionListener delayMQLocalTransactionListener;

    private Type messageType;

}
