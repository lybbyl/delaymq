package com.luoluo.redis.consumer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName Counter
 * @Description: TODO
 * @Author luoluo
 * @Date 2020/8/22
 * @Version V1.0
 **/
public class RandomBoolean {

    private static AtomicInteger counter = new AtomicInteger(15);

    private static Random random = new Random();

    public static boolean getRandomBool() {
        int i = random.nextInt(100) + 1;
        if (counter.getAndIncrement() % i == 0) {
            return true;
        }
        return false;
    }

    public static int getRandom() {
        int i = random.nextInt(200) + 1;
        return i;
    }
}
