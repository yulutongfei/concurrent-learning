package com.sunxu;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/15 09:19
 */
public class ConcurrentHashMapDemo {

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("k1", "v1");
        System.out.println(map.get("k1"));
    }
}
