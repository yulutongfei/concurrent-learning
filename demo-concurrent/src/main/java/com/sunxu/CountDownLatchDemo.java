package com.sunxu;

import java.util.concurrent.CountDownLatch;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/28 15:24
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
    }
}
