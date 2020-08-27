package com.luoluo.delaymq.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 参数不合法
     */
    ILLEGAL_ARG(1, "参数不合法"),
    /**
     * 业务逻辑异常
     */
    BIZ_ERROR(2, "业务逻辑异常"),
    /**
     * 不可自动创建topic
     */
    CANNOT_AUTO_CREATE_TOPIC(3,"不可自动创建topic"),
    /**
     * producer未启动
     */
    PRODUCER_NOT_START(4,"producer未启动")
    ;

    @Getter
    Integer code;

    @Getter
    String description;

    /**
     * 根据 code 返回 description
     *
     * @param code
     * @return
     */
    public static String getDescription(Integer code) {
        for (ErrorCode orderStatus : values()) {
            if (orderStatus.getCode().equals(code)) {
                return orderStatus.description;
            }
        }
        return null;
    }

}
