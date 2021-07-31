package com.sunxu;

import java.util.LinkedList;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/19 23:13
 */
public class MyQueue {

    public static final int MAX_SIZE = 100;

    private LinkedList<String> queue = new LinkedList<>();

    public synchronized void offer(String element) {
        try {
            while (queue.size() == MAX_SIZE) {
                // 一个线程只要执行到这一步,已经获取了一把锁
                // 就是说线程进入一个等待的状态,释放掉锁
                wait();
            }
            queue.addLast(element);
            notifyAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized String take() {
        String element = null;
        try {
            while (queue.size() == 0) {
                wait();
            }
            element = queue.removeFirst();
            notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return element;
    }
}
