
package com.luoluo.delaymq;

/**
 * @description 声明周期接口
 */
public interface Lifecycle {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 是否正在运行
     */
    boolean isRunning();
}
