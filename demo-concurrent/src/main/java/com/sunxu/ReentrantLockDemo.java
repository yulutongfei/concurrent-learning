package com.sunxu;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/25 15:59
 */
public class ReentrantLockDemo {

    private static int i = 0;

    static ReentrantLock lock = new ReentrantLock();


    public static void main(String[] args) {
        new Thread(() -> {
            try {
                lock.lock();
                Condition condition = lock.newCondition();
                condition.await();
                condition.signalAll();
                for (int i1 = 0; i1 < 10; i1++) {
                    i++;
                    System.out.println(i);
                }
            } finally {
                lock.unlock();
            }
        }).start();

        new Thread(() -> {
            try {
                lock.lock();
                for (int i1 = 0; i1 < 10; i1++) {
                    i++;
                    System.out.println(i);
                }
            } finally {
                lock.unlock();
            }
        }).start();
    }
}
