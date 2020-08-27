package com.luoluo.delaymq.exception;

import com.luoluo.delaymq.constant.ErrorCode;

/**
 * @author luoluo
 * Date: 2020/07/20
 **/
public class BizException extends BaseCustomException {

    public BizException(String message) {
        super(message);
        this.code = ErrorCode.BIZ_ERROR.getCode();
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.BIZ_ERROR.getCode();
    }

    public BizException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode code) {
        super(code.getDescription());
        this.code = code.getCode();
    }

    public BizException(String message, Throwable cause, int code) {
        super(message, cause, code);
    }

}
