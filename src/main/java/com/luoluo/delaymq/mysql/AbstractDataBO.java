package com.luoluo.delaymq.mysql;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @Date: 2020/07/20
 * @Author: luoluo
 * 表结构基本数据
 */
@Data
public abstract class AbstractDataBO {

    @Id
    private String id;

    private Long version;

    private Date createdTime;

    private Date updatedTime;
}
