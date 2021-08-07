package com.sunxu;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/25 15:59
 */
public class ReentrantLockDemo {

    private static int i = 0;

    static ReentrantLock lock = new ReentrantLock();


    public static void main(String[] args) throws Exception {
        lock.lock();
        try {
            System.out.println("test");
        } finally {
            lock.unlock();
        }
    }
}
