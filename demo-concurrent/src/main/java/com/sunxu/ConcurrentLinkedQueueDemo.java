package com.sunxu;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/18 01:45
 */
public class ConcurrentLinkedQueueDemo {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.offer("张三");
        queue.offer("李四");
        queue.offer("王五");
        System.out.println(queue.poll());
        System.out.println(queue);
    }
}
