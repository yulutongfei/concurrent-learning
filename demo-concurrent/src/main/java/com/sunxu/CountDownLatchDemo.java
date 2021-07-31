package com.sunxu;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/28 15:24
 */
public class CountDownLatchDemo {

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(10);

        Semaphore semaphore = new Semaphore(3);
        semaphore.acquire();
        semaphore.release();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
    }
}
