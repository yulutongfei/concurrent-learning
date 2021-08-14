package com.sunxu;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 23:05
 * 一下子对同一个数据不同机器上的多个副本，全部分配一个计算任务，谁先计算成功
 * 就采用谁的计算结果就可以了。避免某一台机器因为cpu，内存，磁盘慢读写，导致整体进度过慢
 */
public class SpeculationComputeDemo {

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep((new Random().nextInt(10) + 1) * 1000);
                    System.out.println(Thread.currentThread() + "分配同一个计算任务给不同机器。。。");
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        semaphore.acquire(1);
        System.out.println("一台机器已经执行完毕了，此时可以收集计算任务");
    }
}
