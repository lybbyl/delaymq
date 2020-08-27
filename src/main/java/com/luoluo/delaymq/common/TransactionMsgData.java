package com.luoluo.delaymq.common;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassName TransactionMsgData
 * @Description: 记录事务消息重试次数
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMsgData implements Serializable {

    private int retryCount;

}
