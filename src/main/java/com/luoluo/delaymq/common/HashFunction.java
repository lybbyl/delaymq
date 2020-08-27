package com.luoluo.delaymq.common;

/**
 * Hash String to long value
 * hash 函数
 */
public interface HashFunction {

    long hash(String key);
}
