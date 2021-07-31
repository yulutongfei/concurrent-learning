package com.sunxu;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/26 13:18
 */
public class ReentrantReadWriteLockDemo {

    public static void main(String[] args) {
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

        writeLock.lock();
        writeLock.unlock();
        readLock.lock();
        readLock.unlock();
    }
}
