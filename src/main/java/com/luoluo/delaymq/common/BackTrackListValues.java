package com.luoluo.delaymq.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassNamackTrack
 * @Description: 用来回溯值的数据结构 线程安全
 * @Author luoluo
 * @Date 2020/7/16
 * @Version V1.0
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
public class BackTrackListValues<T> {

    private CopyOnWriteArrayList<T> trackValues;

    private LinkedList<List<T>> forwardList;

    private int size;

    private int initialCapacity;

    public BackTrackListValues(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        this.size = 0;
        this.initialCapacity = initialCapacity;
        forwardList = new LinkedList();
        trackValues = new CopyOnWriteArrayList();
    }

    public List<T> getAllValue() {
        return trackValues;
    }

    public synchronized void addValue(List<T> values) {
        if (size == initialCapacity) {
            List<T> poll = forwardList.remove();
            if (poll != null) {
                trackValues.removeAll(poll);
            }
            forwardList.addLast(values);
            trackValues.addAll(values);
        } else {
            size++;
            forwardList.addLast(values);
            trackValues.addAll(values);
        }
    }

}
