package com.luoluo.delaymq.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * JSON 工具类
 */
public class JSONUtil {

    private static Gson gson =new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static <T> T parseObject(String text, Class<T> clazz) {
        return gson.fromJson(text,clazz);
    }

    public static <T> T parseObject(String text, Type type) {
        return gson.fromJson(text,type);
    }

    public static String toJSONString(Object javaObject) {
        return gson.toJson(javaObject);
    }

    public static byte[] toJSONBytes(Object javaObject) {
        return gson.toJson(javaObject).getBytes();
    }

    public static <T> T parseObject(Object body, Class<T> clazz) {
        return gson.fromJson(gson.toJson(body),clazz);
    }
}
