package com.luoluo.delaymq.exception;

/**
 * @author luoluo
 * Date: 2020/07/20
 **/
public class BaseCustomException extends RuntimeException {
    protected int code;

    public BaseCustomException(String message) {
        super(message);
    }

    public BaseCustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseCustomException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
