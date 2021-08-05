package com.sunxu;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/5 21:15
 */
public class ThreadLocalDemo {

    public static void main(String[] args) {
        ThreadLocal<Long> requestId = new ThreadLocal<>();

        new Thread(() -> {
            requestId.set(0L);
        }).start();
    }
}
