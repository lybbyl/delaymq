package com.luoluo.delaymq.mysql;

import lombok.*;

/**
 * @ClassName ConsumerMsgData
 * @Description: 消费数据记录
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
public class MessageStore extends AbstractDataBO {

    private String messageValue;

}
