package com.luoluo.delaymq.lock;


/**
 * @ClassName DistributedLock
 * @Description: 分布式锁
 * @Author luoluo
 * @Date 2020/7/20
 * @Version V1.0
 **/
public interface DistributedLock {

    /**
     * 尝试加锁
     *
     * @param key 加锁的key
     * @return 尝试加锁成功返回true; 失败返回false
     */
    boolean tryLock(String key);

    /**
     * 尝试加锁
     *
     * @param key     加锁的key
     * @param timeout 超时时间
     * @return 尝试加锁成功返回true; 失败返回false
     */
    boolean tryLock(String key, long timeout);

    /**
     * 解锁操作
     *
     * @param key 解锁的key
     * @return 解锁成功返回true; 失败返回false
     */
    void unlock(String key);
}
