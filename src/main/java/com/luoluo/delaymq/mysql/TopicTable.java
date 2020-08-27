package com.luoluo.delaymq.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName Topic表记录
 * @Description: 消费数据记录
 * @Author luoluo
 * @Date 2020/7/9
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicTable extends AbstractDataBO {

    private String topicData;

}
