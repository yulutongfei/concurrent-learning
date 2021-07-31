package com.sunxu;

import java.util.concurrent.locks.LockSupport;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/25 19:18
 */
public class ParkDemo {

    public static void main(String[] args) throws InterruptedException {

        Object obj = new Object();
        Thread thread1 = new Thread(() -> {
            System.out.println("挂起前操作");
            LockSupport.park(obj);
            System.out.println("挂起后操作");
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            LockSupport.unpark(thread1);
        });
        thread2.start();

        Thread.sleep(10000);
    }
}
