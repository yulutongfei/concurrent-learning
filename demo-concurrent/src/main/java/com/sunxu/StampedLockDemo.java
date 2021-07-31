package com.sunxu;

import java.util.concurrent.locks.StampedLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/29 16:40
 */
public class StampedLockDemo {

    public static void main(String[] args) {
        StampedLock stampedLock = new StampedLock();

        long stamp = stampedLock.writeLock();
        stampedLock.unlockWrite(stamp);
    }
}
