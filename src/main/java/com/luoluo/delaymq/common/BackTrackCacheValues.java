package com.luoluo.delaymq.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName BackTrack
 * @Description: 用来回溯值的数据结构 线程安全
 * @Author luoluo
 * @Date 2020/7/16
 * @Version V1.0
 **/
@Getter
@Setter
public class BackTrackCacheValues<T> {

    private int time;

    /**
     * 通过CacheBuilder构建一个缓存实例
     */
    Cache<Integer, List<T>> cache;

    private AtomicInteger key = new AtomicInteger(0);

    public BackTrackCacheValues(int time) {
        this.time = time;
        cache = CacheBuilder.newBuilder()
                // 设置缓存在写入time秒后失效
                .expireAfterWrite(time, TimeUnit.SECONDS)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public List<T> getAllValue() {
        List<T> list = new ArrayList<>();
        for (List<T> value : cache.asMap().values()) {
            list.addAll(value);
        }
        return list;
    }

    public void addValue(List<T> values) {
        if (values != null && values.size() > 0) {
            int andIncrement = key.getAndIncrement();
            cache.put(andIncrement, values);
        }
    }
}
