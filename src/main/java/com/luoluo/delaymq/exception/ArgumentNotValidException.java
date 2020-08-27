package com.luoluo.delaymq.exception;

import com.luoluo.delaymq.constant.ErrorCode;

/**
 * @ClassName
 * @Description: 错误类
 * @Author luoluo
 * @Date 2020/7/10
 * @Version V1.0
 **/
public class ArgumentNotValidException extends BaseCustomException {
    public ArgumentNotValidException(String message) {
        super(message);
        this.code = ErrorCode.ILLEGAL_ARG.getCode();
    }

    public ArgumentNotValidException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ArgumentNotValidException(String message, Throwable cause, int code) {
        super(message, cause, code);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
