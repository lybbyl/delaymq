package com.luoluo.delaymq.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
@NoArgsConstructor
public class BackTrackSetValues<T> {

    private CopyOnWriteArraySet<T> trackValues;

    private LinkedList<Set<T>> forwardList;

    private int size;

    private int initialCapacity;

    public BackTrackSetValues(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        this.size = 0;
        this.initialCapacity = initialCapacity;
        forwardList = new LinkedList();
        trackValues = new CopyOnWriteArraySet<>();
    }

    public Set<T> getAllValue() {
        return trackValues;
    }

    public synchronized Set<T> addValue(Set<T> values) {
        if (size == initialCapacity) {
            Set<T> poll = forwardList.remove();
            if (poll != null) {
                trackValues.removeAll(poll);
            }
            forwardList.addLast(values);
            trackValues.addAll(values);
            return poll;
        } else {
            size++;
            forwardList.addLast(values);
            trackValues.addAll(values);
            return null;
        }
    }

    public void addTrackValue(Set<T> values) {
        trackValues.addAll(values);
    }
}
