package com.sunxu;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/16 01:22
 */
public class ThreadUnsafeDemo {

    private static int i = 0;

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            for (int n = 0; n < 100000; n++) {
//                synchronized (ThreadUnsafeDemo.class) {

                    i++;
//                }
            }
        };
        Thread thread = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        thread.start();
        thread2.start();
        thread.join();
        thread2.join();
        System.out.println(i);
    }
}
