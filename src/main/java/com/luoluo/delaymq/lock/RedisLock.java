package com.luoluo.delaymq.lock;

import com.luoluo.delaymq.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisLock
 * @Description: redis分布式锁
 * @Author luoluo
 * @Date 2020/7/8
 * @Version V1.0
 **/
@Slf4j
public class RedisLock implements DistributedLock {

    /**
     * todo  不借助redis 使用lua脚本实现分布式锁
     */
    private RedisUtils redisUtils;

    private RedissonClient redissonClient;

    public RedisLock(RedisUtils redisUtils, RedissonClient redissonClient) {
        this.redisUtils = redisUtils;
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(String key) {
        RLock rLock = redissonClient.getLock(key);
        // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
        boolean res = false;
        try {
            res = rLock.tryLock();
        } catch (Exception e) {
            log.error("Redis lock is abnormal", e);
        }
        return res;
    }

    @Override
    public boolean tryLock(String key, long timeout) {
        RLock rLock = redissonClient.getLock(key);
        // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
        boolean res = false;
        try {
            res = rLock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Redis lock is abnormal", e);
        }
        return res;
    }

    @Override
    public void unlock(String key) {
        try {
            RLock rLock = redissonClient.getLock(key);
            rLock.unlock();
        } catch (Exception e) {
            log.debug("Redis Distributed Lock Unlocking is abnormal", e);
        }
    }
}
