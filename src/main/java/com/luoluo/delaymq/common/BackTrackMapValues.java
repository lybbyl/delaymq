package com.luoluo.delaymq.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName BackTrack
 * @Description: 用来回溯值的数据结构 线程安全
 * @Author luoluo
 * @Date 2020/7/16
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
public class BackTrackMapValues<T> {

    private CopyOnWriteArrayList<ConcurrentHashMap<String, T>> forwardList;

    private int size;

    private int initialCapacity;

    public BackTrackMapValues(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        this.size = 0;
        this.initialCapacity = initialCapacity;
        forwardList = new CopyOnWriteArrayList<>();
    }

    public ConcurrentHashMap<String, T> getAllValue() {
        ConcurrentHashMap<String, T> map = new ConcurrentHashMap();
        for (ConcurrentHashMap<String, T> back : forwardList) {
            map.putAll(back);
        }
        return map;
    }

    public synchronized void addValue(ConcurrentHashMap<String, T> values) {
        if (size == initialCapacity) {
            forwardList.remove(0);
            forwardList.add(values);
        } else {
            size++;
            forwardList.add(values);
        }
    }

    public synchronized void addTrackValue(String key, T value) {
        for (Map<String, T> back : forwardList) {
            back.remove(key);
        }
        forwardList.get(size - 1).putIfAbsent(key, value);
    }

    public T getValue(String key) {
        for (Map<String, T> back : forwardList) {
            if (back.containsKey(key)) {
                return back.get(key);
            }
        }
        return null;
    }
}
