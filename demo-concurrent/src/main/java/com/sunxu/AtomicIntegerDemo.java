package com.sunxu;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/21 23:03
 */
public class AtomicIntegerDemo {

    static Integer i = 0;

    static AtomicInteger j = new AtomicInteger(0);

    public static void main(String[] args) {
        synchronizeAdd();

    }

    private static void synchronizeAdd() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                AtomicIntegerDemo.i++;
            }).start();
        }
    }
}
